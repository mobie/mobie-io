/*-
 * #%L
 * Readers and writers for image data
 * %%
 * Copyright (C) 2021 - 2024 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.io;

import bdv.cache.SharedQueue;
import ij.IJ;
import ij.Prefs;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.imagedata.*;


public class ImageDataOpener
{
    private static final String ZARR_OPENER_PREF_KEY = "mobie.io.zarrOpener";

    // Mutable on purpose: can be overridden at runtime after auto-detection.
    private static volatile ZarrOpener zarrOpener = loadPersistedZarrOpener();

    static {

        // Disable Bio-Formats logging
        DebugTools.setRootLevel( "OFF" );

        // Needed for NetApp EMBL S3 Store
        System.setProperty(
                "com.amazonaws.services.s3.disableGetObjectMD5Validation",
                "true"
        );
    }

    private static ZarrOpener detectZarrOpener()
    {
        return ZarrOpener.OME_Zarr_Zarr_Java.isAvailable() ? ZarrOpener.OME_Zarr_Zarr_Java : ZarrOpener.MOBIE_N5;
    }

    /**
     * The configured opener if this Fiji has its classes, otherwise {@link ZarrOpener#MOBIE_N5}.
     * The configured opener stays persisted, so enabling the OME-Zarr update site restores it.
     */
    private static ZarrOpener effectiveZarrOpener()
    {
        if ( zarrOpener.isAvailable() )
            return zarrOpener;

        if ( !fallbackLogged )
        {
            fallbackLogged = true;
            IJ.log( "[WARNING] The OME-Zarr reader " + zarrOpener + " is not available in this Fiji installation, using "
                    + ZarrOpener.MOBIE_N5 + " instead. Enable the OME-Zarr update site (Help > Update) to use it." );
        }
        return ZarrOpener.MOBIE_N5;
    }

    private static ZarrOpener loadPersistedZarrOpener()
    {
        final String persistedOpener = Prefs.get( ZARR_OPENER_PREF_KEY, null );

        if ( persistedOpener != null )
        {
            try
            {
                return ZarrOpener.valueOf( persistedOpener );
            }
            catch ( IllegalArgumentException ignored )
            {
                // Ignore invalid legacy values and fall back to auto-detection.
            }
        }

        return detectZarrOpener();
    }

    public static ZarrOpener getZarrOpener()
    {
        return zarrOpener;
    }

    public static void setZarrOpener( ZarrOpener opener )
    {
        if ( opener == null )
            throw new IllegalArgumentException( "opener must not be null" );

        zarrOpener = opener;
        Prefs.set( ZARR_OPENER_PREF_KEY, opener.name() );
    }

    public static void autoConfigureZarrOpener()
    {
        setZarrOpener( detectZarrOpener() );
    }

    public enum ZarrOpener
    {
        MOBIE_N5,
        // The OME-Zarr update site provides these. Probing ome.zarr.fiji.read.OmeZarr also catches
        // an installation that has a backend jar but not ome-zarr-fiji
        OME_Zarr_N5( "ome.zarr.fiji.read.OmeZarr", "ome.zarr.n5.N5PyramidBackend" ),
        // the zarr-java jar may be missing while ome-zarr-zarrjava is present.
        OME_Zarr_Zarr_Java( "ome.zarr.fiji.read.OmeZarr", "ome.zarr.zarrjava.ZarrJavaPyramidBackend", "dev.zarr.zarrjava.core.Array" );

        private final String[] requiredClasses;
        private Boolean available;

        ZarrOpener( String... requiredClasses )
        {
            this.requiredClasses = requiredClasses;
        }

        /**
         * Whether this Fiji installation has the classes this opener needs. Probed once per session.
         */
        public synchronized boolean isAvailable()
        {
            if ( available == null )
                available = probe();
            return available;
        }

        private boolean probe()
        {
            try
            {
                for ( String className : requiredClasses )
                    Class.forName( className, false, ImageDataOpener.class.getClassLoader() );
                return true;
            }
            catch ( ClassNotFoundException | LinkageError e )
            {
                return false;
            }
        }
    }

    private static boolean fallbackLogged;

    /**
     * Tells the user that an OME-Zarr reader failed on a missing or outdated class, e.g. an
     * OME-Zarr update site that is disabled or older than this MoBIE.
     */
    public static void showOmeZarrReaderUnavailable( LinkageError error )
    {
        final String title = "MoBIE: OME-Zarr reader unavailable";
        final String message = "The selected OME-Zarr reader could not be loaded in this Fiji installation.\n"
                + "Missing or outdated: " + error.getMessage() + "\n\n"
                + "Please enable or update the OME-Zarr update site (Help > Update) and restart Fiji,\n"
                + "or choose the MoBIE N5 reader in Plugins > MoBIE > Settings > Configure MoBIE Settings.";
        try
        {
            IJ.showMessage( title, message );
        }
        catch ( Throwable ignored )
        {
            System.err.println( title + ": " + message );
        }
    }


    /*
    If you only have the URI use:
    ImageDataFormat imageDataFormat = ImageDataFormat.fromPath( uri ),
    SharedQueue sharedQueue = new SharedQueue( 1 )
     */
    public static < T extends NumericType< T > & NativeType< T > > ImageData< T > open(
            String uri,
            ImageDataFormat imageDataFormat,
            SharedQueue sharedQueue )
    {
        switch (imageDataFormat)
        {
            case OmeZarr:
            case OmeZarrS3:
                if ( imageDataFormat.getSecretAndAccessKey() == null )
                {
                    switch ( effectiveZarrOpener() )
                    {
                        case MOBIE_N5:
                            return new N5ImageData<>( uri, sharedQueue );
                        case OME_Zarr_N5:
                            return new PyramidalN5ImageData<>(  uri, sharedQueue );
                        case OME_Zarr_Zarr_Java:
                            return new PyramidalZarrJavaImageData<>(   uri, sharedQueue );
                    }
                }
                else
                {
                    // use this, because that's currently the only one that can handle the keys
                    return new N5ImageData<>( uri, sharedQueue, imageDataFormat.getSecretAndAccessKey() );
                }
            case OpenOrganelleS3:
            case N5:
                return new N5ImageData<>( uri, sharedQueue, imageDataFormat.getSecretAndAccessKey() );
            case Toml:
                return new TOMLImageData<>( uri, sharedQueue );
            case Tiff:
                return new TIFFImageData<>( uri, sharedQueue );
            case ImageJ:
                return new ImageJImageData<>( uri, sharedQueue );
            case Imaris:
                return new ImarisImageData<>( uri, sharedQueue );
            case Ilastik:
                return new IlastikImageData<>( uri, sharedQueue );
            case BioFormats:
                return new BioFormatsImageData<>( uri, sharedQueue );
            case BioFormatsS3:
                // TODO: use the s3AccessAndSecretKey
                return new BioFormatsS3ImageData<>( uri, sharedQueue );
            case Bdv:
            case BdvHDF5:
            case BdvN5:
            case BdvN5S3:
                // TODO: use the s3AccessAndSecretKey
                return new BDVXMLImageData<>( uri, sharedQueue );
            case BdvOmeZarr:
            case BdvOmeZarrS3:
            default:
                throw new RuntimeException( "Opening " + imageDataFormat + " is not supported; " +
                        "if you need it please report here: " +
                        "https://github.com/mobie/mobie-io/issues" );
        }
    }
}
