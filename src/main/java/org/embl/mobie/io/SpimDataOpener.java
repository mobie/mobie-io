/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2022 EMBL
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
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import ch.epfl.biop.bdv.img.CacheControlOverride;
import ch.epfl.biop.bdv.img.OpenersToSpimData;
import ch.epfl.biop.bdv.img.bioformats.BioFormatsHelper;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ch.epfl.biop.bdv.img.opener.OpenerSettings;
import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import net.imglib2.util.Cast;
import org.embl.mobie.io.n5.openers.N5Opener;
import org.embl.mobie.io.n5.openers.N5S3Opener;
import org.embl.mobie.io.ome.zarr.loaders.N5S3OMEZarrImageLoader;
import org.embl.mobie.io.ome.zarr.loaders.xml.XmlN5OmeZarrImageLoader;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrOpener;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.embl.mobie.io.openorganelle.OpenOrganelleS3Opener;
import org.embl.mobie.io.toml.TOMLOpener;
import org.embl.mobie.io.util.CustomXmlIoSpimData;
import org.embl.mobie.io.util.IOHelper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static mpicbg.spim.data.XmlKeys.IMGLOADER_TAG;
import static mpicbg.spim.data.XmlKeys.SEQUENCEDESCRIPTION_TAG;

@Slf4j
public class SpimDataOpener {

    public static final String ERROR_WHILE_TRYING_TO_READ_SPIM_DATA = "Error while trying to read spimData";

    public SpimDataOpener() {
    }

    public AbstractSpimData< ? > open(String imagePath, ImageDataFormat imageDataFormat, SharedQueue sharedQueue) throws UnsupportedOperationException, SpimDataException {
        if (sharedQueue == null)
            return open(imagePath, imageDataFormat);
        return openWithSharedQueue(imagePath, imageDataFormat, sharedQueue);
    }

    public AbstractSpimData< ? > open( String imagePath, ImageDataFormat imageDataFormat) throws UnsupportedOperationException, SpimDataException {
        switch (imageDataFormat) {
            case Toml:
                return new TOMLOpener( imagePath ).asSpimData();
            case Tiff:
                final File file = new File(imagePath);
                return open((new Opener()).openTiff( file.getParent(), file.getName()));
            case ImageJ:
                return open(IJ.openImage(imagePath));
            case BioFormats:
                return openWithBioFormats(imagePath);
            case Imaris:
                return openImaris(imagePath);
            case Bdv:
            case BdvHDF5:
            case BdvN5:
            case BdvN5S3:
                return openBdvXml(imagePath);
            case OmeZarr:
                return openOmeZarr(imagePath);
            case OmeZarrS3:
                return openOmeZarrS3(imagePath);
            case BdvOmeZarr:
                return openBdvOmeZarr(imagePath, null);
            case BdvOmeZarrS3:
                return openBdvOmeZarrS3(imagePath, null);
            case OpenOrganelleS3:
                return openOpenOrganelleS3(imagePath);
            default:
                throw new UnsupportedOperationException("Opening of " + imageDataFormat + " is not supported.");
        }
    }

    private AbstractSpimData< ? > openWithSharedQueue( String imagePath, ImageDataFormat imageDataFormat, SharedQueue sharedQueue) throws UnsupportedOperationException, SpimDataException {
        switch (imageDataFormat) {
            case Toml:
                return new TOMLOpener( imagePath ).asSpimData(sharedQueue);
            case Tiff:
                return open(IOHelper.openTiffAsImagePlus( imagePath ), sharedQueue);
            case ImageJ:
                return open(IJ.openImage(imagePath), sharedQueue);
            case BioFormats:
                return openWithBioFormats(imagePath, sharedQueue);
            case BdvN5:
                return openBdvN5(imagePath, sharedQueue);
            case BdvN5S3:
                return openBdvN5S3(imagePath, sharedQueue);
            case OmeZarr:
                return openOmeZarr(imagePath, sharedQueue);
            case OmeZarrS3:
                return openOmeZarrS3(imagePath, sharedQueue);
            case BdvOmeZarr:
                return openBdvOmeZarr(imagePath, sharedQueue);
            case BdvOmeZarrS3:
                return openBdvOmeZarrS3(imagePath, sharedQueue);
            default:
                // open without {@code SharedQueue}
                return open(imagePath, imageDataFormat);
        }
    }

    public AbstractSpimData open( ImagePlus imagePlus )
    {
        return ImagePlusToSpimData.getSpimData( imagePlus );
    }

    public AbstractSpimData open( ImagePlus imagePlus, SharedQueue sharedQueue )
    {
        final AbstractSpimData< ? > spimData = open(  imagePlus );

        setSharedQueue( sharedQueue, spimData );

        return spimData;
    }

    public static void setSharedQueue( SharedQueue sharedQueue, AbstractSpimData< ? > spimData )
    {
        BasicImgLoader imgLoader = spimData.getSequenceDescription().getImgLoader();

        if (imgLoader instanceof CacheControlOverride) {
            CacheControlOverride cco = (CacheControlOverride) imgLoader;
            final VolatileGlobalCellCache volatileGlobalCellCache = new VolatileGlobalCellCache( sharedQueue );
            cco.setCacheControl( volatileGlobalCellCache );
        }
    }

    @NotNull
    private SpimDataMinimal openImaris(String imagePath) throws RuntimeException {
        try {
            return Imaris.openIms(imagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SpimData openBdvXml(String path) throws SpimDataException {
        try {
            InputStream stream = IOHelper.getInputStream(path);
            return new CustomXmlIoSpimData().loadFromStream(stream, path);
        } catch (SpimDataException | IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openBdvN5(String path, SharedQueue queue) throws SpimDataException {
        try {
            return N5Opener.openFile(path, queue);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openBdvN5S3(String path, SharedQueue queue) throws SpimDataException {
        try {
            return N5S3Opener.readURL(path, queue);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openOmeZarr(String path) throws SpimDataException {
        try {
            return OMEZarrOpener.openFile(path);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openOmeZarr(String path, SharedQueue sharedQueue) throws SpimDataException {
        try {
            return OMEZarrOpener.openFile(path, sharedQueue);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openOmeZarrS3(String path) throws SpimDataException {
        try {
            return OMEZarrS3Opener.readURL(path);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openOmeZarrS3(String path, SharedQueue sharedQueue) throws SpimDataException {
        try {
            return OMEZarrS3Opener.readURL(path, sharedQueue);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openOpenOrganelleS3(String path) throws SpimDataException {
        try {
            return OpenOrganelleS3Opener.readURL(path);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openBdvOmeZarrS3(String path, SharedQueue queue) {
        //TODO: finish bug fixing
        try {
            SAXBuilder sax = new SAXBuilder();
            InputStream stream = IOHelper.getInputStream(path);
            Document doc = sax.build(stream);
            Element imgLoaderElem = doc.getRootElement().getChild("SequenceDescription").getChild("ImageLoader");
            String bucketAndObject = imgLoaderElem.getChild("BucketName").getText() + "/" + imgLoaderElem.getChild("Key").getText();
            String[] split = bucketAndObject.split("/");
            String bucket = split[0];
            String object = Arrays.stream(split).skip(1L).collect(Collectors.joining("/"));
            N5S3OMEZarrImageLoader imageLoader;
            if (queue != null) {
                imageLoader = new N5S3OMEZarrImageLoader(imgLoaderElem.getChild("ServiceEndpoint").getText(), imgLoaderElem.getChild("SigningRegion").getText(), bucket, object, ".", queue);
            } else {
                imageLoader = new N5S3OMEZarrImageLoader(imgLoaderElem.getChild("ServiceEndpoint").getText(), imgLoaderElem.getChild("SigningRegion").getText(), bucket, object, ".");
            }
            SpimData spim = new SpimData(null, Cast.unchecked(imageLoader.getSequenceDescription()), imageLoader.getViewRegistrations());
            SpimData spimData;
            try {
                InputStream st = IOHelper.getInputStream(path);
                spimData = (new CustomXmlIoSpimData()).loadFromStream(st, path);
            } catch (SpimDataException exception) {
                log.debug("Failed to load stream from {}", path, exception);
                return null;
            }
            spimData.setBasePath(null);
            spimData.getSequenceDescription().setImgLoader(spim.getSequenceDescription().getImgLoader());
            spimData.getSequenceDescription().getAllChannels().putAll(spim.getSequenceDescription().getAllChannels());
            return spimData;
        } catch (JDOMException | IOException e) {
            log.debug("Failed to open openBdvOmeZarrS3", e);
            return null;
        }
    }

    public AbstractSpimData< ? > openWithBioFormats( String path )
    {
        final File file = new File( path );
        List< OpenerSettings > openerSettings = new ArrayList<>();
        int numSeries = BioFormatsHelper.getNSeries(file);
        for (int i = 0; i < numSeries; i++) {
            openerSettings.add(
                    OpenerSettings.BioFormats()
                            .location(file)
                            .setSerie(i) );
        }
        return OpenersToSpimData.getSpimData( openerSettings );
    }

    public AbstractSpimData< ? > openWithBioFormats( String path, SharedQueue sharedQueue )
    {
        final AbstractSpimData< ? > spimData = openWithBioFormats( path );

        setSharedQueue( sharedQueue, spimData );

        return spimData;
    }

//    public static SpimData asSpimData( AbstractSpimData< ? > abstractSpimData )
//    {
//        final AbstractSequenceDescription< ?, ?, ? > abstractSequenceDescription = abstractSpimData.getSequenceDescription();
//        final SequenceDescription sequenceDescription = new SequenceDescription( abstractSequenceDescription.getTimePoints(), abstractSequenceDescription.getViewSetups() );
//        final SpimData spimData = new SpimData( abstractSpimData.getBasePath(), sequenceDescription, abstractSpimData.getViewRegistrations() );
//        return spimData;
//    }

    private SpimData openBdvOmeZarr(String path, @Nullable SharedQueue sharedQueue) throws SpimDataException {
        SpimData spimData = openBdvXml(path);
        SpimData spimDataWithImageLoader = getSpimDataWithImageLoader(path, sharedQueue);
        if (spimData != null && spimDataWithImageLoader != null) {
            spimData.getSequenceDescription().setImgLoader(spimDataWithImageLoader.getSequenceDescription().getImgLoader());
            spimData.getSequenceDescription().getAllChannels().putAll(spimDataWithImageLoader.getSequenceDescription().getAllChannels());
            return spimData;
        } else {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA);
        }
    }

    @NotNull
    private N5S3OMEZarrImageLoader createN5S3OmeZarrImageLoader(String path, @Nullable SharedQueue queue) throws IOException, JDOMException {
        final SAXBuilder sax = new SAXBuilder();
        InputStream stream = IOHelper.getInputStream(path);
        final Document doc = sax.build(stream);
        final Element imgLoaderElem = doc.getRootElement().getChild(SEQUENCEDESCRIPTION_TAG).getChild(IMGLOADER_TAG);
        String bucketAndObject = imgLoaderElem.getChild("BucketName").getText() + "/" + imgLoaderElem.getChild("Key").getText();
        final String[] split = bucketAndObject.split("/");
        String bucket = split[0];
        String object = Arrays.stream(split).skip(1).collect(Collectors.joining("/"));
        if (queue == null) {
            return new N5S3OMEZarrImageLoader(imgLoaderElem.getChild("ServiceEndpoint").getText(), imgLoaderElem.getChild("SigningRegion").getText(), bucket, object, ".");
        } else {
            return new N5S3OMEZarrImageLoader(imgLoaderElem.getChild("ServiceEndpoint").getText(), imgLoaderElem.getChild("SigningRegion").getText(), bucket, object, ".", queue);
        }
    }

    private SpimData getSpimDataWithImageLoader(String path, @Nullable SharedQueue sharedQueue) {
        try {
            final SAXBuilder sax = new SAXBuilder();
            InputStream stream = IOHelper.getInputStream(path);
            final Document doc = sax.build(stream);
            final Element imgLoaderElem = doc.getRootElement().getChild(SEQUENCEDESCRIPTION_TAG).getChild(IMGLOADER_TAG);
            String imagesFile = XmlN5OmeZarrImageLoader.getDatasetsPathFromXml(imgLoaderElem, path);
            if (imagesFile != null) {
                if (new File(imagesFile).exists()) {
                    return sharedQueue != null ? OMEZarrOpener.openFile(imagesFile, sharedQueue)
                        : OMEZarrOpener.openFile(imagesFile);
                } else {
                    return sharedQueue != null ? OMEZarrS3Opener.readURL(imagesFile, sharedQueue)
                        : OMEZarrS3Opener.readURL(imagesFile);
                }
            }
        } catch (JDOMException | IOException e) {
            IJ.log(e.getMessage());
        }
        return null;
    }
}
