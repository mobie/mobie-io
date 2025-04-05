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

import ij.IJ;
import ij.ImagePlus;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import net.thisptr.jackson.jq.internal.misc.Strings;
import org.embl.mobie.io.util.IOHelper;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.ij.N5Importer;
import org.janelia.saalfeldlab.n5.ij.N5ScalePyramidExporter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.janelia.saalfeldlab.n5.ij.N5ScalePyramidExporter.GZIP_COMPRESSION;
import static org.janelia.saalfeldlab.n5.ij.N5ScalePyramidExporter.ZARR_FORMAT;

public class OMEZarrWriter
{
    public enum ImageType
    {
        Intensities,
        Labels;
    }

    public static void write( ImagePlus imp, String uri, ImageType imageType, boolean overwrite )
    {
        N5ScalePyramidExporter.DOWNSAMPLE_METHOD downSampleMethod =
                imageType.equals( ImageType.Labels ) ?
                        N5ScalePyramidExporter.DOWNSAMPLE_METHOD.Sample
                        : N5ScalePyramidExporter.DOWNSAMPLE_METHOD.Average;

        // TODO: https://github.com/saalfeldlab/n5-ij/issues/82
        String chunkSizeArg = getChunkSizeArg( imp );

        IJ.log("Writing data to: " + uri );
        IJ.log("Chunking set to: " + chunkSizeArg );

        try
        {
            N5URI n5URI = new N5URI( uri );
            String containerPath = n5URI.getContainerPath();
            String groupPath = n5URI.getGroupPath();

            N5ScalePyramidExporter exporter = new N5ScalePyramidExporter(
                    imp,
                    containerPath,
                    groupPath,
                    ZARR_FORMAT,
                    chunkSizeArg,
                    true,
                    downSampleMethod,
                    N5Importer.MetadataOmeZarrKey,
                    GZIP_COMPRESSION
            );

            // TODO: https://github.com/saalfeldlab/n5-ij/issues/83
            Field nThreads = N5ScalePyramidExporter.class.getDeclaredField( "nThreads" );
            nThreads.setAccessible( true );
            nThreads.setInt( exporter, Runtime.getRuntime().availableProcessors() - 1 );

            exporter.setOverwrite( overwrite );

            // TODO: Log progress: https://github.com/saalfeldlab/n5-ij/issues/84
            exporter.run();

            // If available, add Bio-Formats metadata
            // https://forum.image.sc/t/create-ome-xml-when-creating-ome-zarr-in-fiji/110683
            String xml = IOHelper.getOMEXml( imp );
            if ( xml != null )
            {
                IJ.log( "Found OME Metadata in image." );

                if ( ! checkMetadataConsistency( imp, xml ) ) return;

                new File( uri, "OME"  ).mkdirs();
                String omeXmlPath = IOHelper.combinePath( uri, "OME", "METADATA.ome.xml" );
                FileWriter writer = new FileWriter( omeXmlPath );
                writer.write( xml );
                writer.close();
                IJ.log( "OME Metadata added to OME-Zarr." );
            }
        }
        catch ( URISyntaxException | NoSuchFieldException | IllegalAccessException |
                IOException | ServiceException | DependencyException e )
        {
            throw new RuntimeException( e );
        }

        // TODO: If we wanted to give the dataset a name we also have to
        //       update how we refer to such an image or segmentation in the dataset.JSON
        //       String n5Dataset = "";
//        String n5Dataset = imageType.equals( ImageType.Labels ) ? "labels" : "intensities";
//        if ( imageType.equals( ImageType.Labels ) )
//        {
//            uri = IOHelper.combinePath( uri, "labels/0" );
//        }
//        else
//        {
//            uri = IOHelper.combinePath( uri, "intensities" );
//        }
    }

    private static boolean checkMetadataConsistency( ImagePlus imp, String xml ) throws DependencyException, ServiceException
    {
        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance( OMEXMLService.class );
        OMEXMLMetadata metadata = service.createOMEXMLMetadata( xml );
        if ( metadata.getPixelsSizeX(0).getNumberValue().intValue() != imp.getWidth()
            || metadata.getPixelsSizeY(0).getNumberValue().intValue() != imp.getHeight()
            || metadata.getPixelsSizeZ(0).getNumberValue().intValue() != imp.getNSlices()
            || metadata.getPixelsSizeC(0).getNumberValue().intValue() != imp.getNChannels()
            || metadata.getPixelsSizeT(0).getNumberValue().intValue() != imp.getNFrames() )
        {
            IJ.log( "Image dimensions do not equal metadata dimension;\n" +
                    "OME Metadata will thus not be saved." );
            return false;
        }
        return true;
    }

    @NotNull
    private static String getChunkSizeArg( ImagePlus imp )
    {
        // init the chunks
        ArrayList< String > chunks = new ArrayList<>();
        chunks.add( "96" ); // 0 = x
        chunks.add( "96" ); // 1 = y
        chunks.add( "1" ); // 2 = c
        chunks.add( "96" ); // 3 = z
        chunks.add( "1" ); // 4 = t

        // remove singleton dimensions, as required by the N5ScalePyramidExporter
        if ( imp.getNFrames() == 1 ) chunks.remove( 4 );

        if ( imp.getNSlices() == 1 )
        {
            chunks.remove( 3 );
            // since this is 2-D data, make the chunks in xy larger
            chunks.set( 0, "1024" );
            chunks.set( 1, "1024" );
        }

        if ( imp.getNChannels() == 1 ) chunks.remove( 2 );

        return Strings.join( ",", chunks );
    }

}
