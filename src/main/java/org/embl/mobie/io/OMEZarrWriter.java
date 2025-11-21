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

import ij.ImagePlus;
import net.thisptr.jackson.jq.internal.misc.Strings;
import org.embl.mobie.io.util.ChunkSizeComputer;
import org.embl.mobie.io.util.IOHelper;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.ij.N5Importer;
import org.janelia.saalfeldlab.n5.ij.N5ScalePyramidExporter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
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

    // auto-chunking
    public static void write(
            ImagePlus imp,
            String uri,
            ImageType imageType,
            boolean overwrite )
    {
        write(
                imp,
                uri,
                imageType,
                new ChunkSizeComputer( imp.getDimensions(), imp.getBytesPerPixel() ).getChunkDimensionsXYCZT( 8000000 ),
                overwrite
        );
    }

    // configurable chunking
    public static void write(
            ImagePlus imp,
            String uri,
            ImageType imageType,
            int[] chunkDimensionsXYCZT,
            boolean overwrite )
    {
        N5ScalePyramidExporter.DOWNSAMPLE_METHOD downSampleMethod =
                imageType.equals( ImageType.Labels ) ?
                        N5ScalePyramidExporter.DOWNSAMPLE_METHOD.Sample
                        : N5ScalePyramidExporter.DOWNSAMPLE_METHOD.Average;

        try
        {
            N5URI n5URI = new N5URI( uri );
            String containerPath = n5URI.getContainerPath();
            String groupPath = n5URI.getGroupPath();
            String n5ChunkSizeArg = getN5ChunkSizeArg( imp.getDimensions(), chunkDimensionsXYCZT );

            N5ScalePyramidExporter exporter = new N5ScalePyramidExporter(
                    imp,
                    containerPath,
                    groupPath,
                    ZARR_FORMAT,
                    n5ChunkSizeArg,
                    true,
                    downSampleMethod,
                    N5Importer.MetadataOmeZarrKey,
                    GZIP_COMPRESSION
            );

            exporter.setNumThreads( Runtime.getRuntime().availableProcessors() - 1 );
            exporter.setOverwrite( overwrite );

            // TODO: Log progress: https://github.com/saalfeldlab/n5-ij/issues/84
            exporter.run();

            // If available, add Bio-Formats metadata
            // https://forum.image.sc/t/create-ome-xml-when-creating-ome-zarr-in-fiji/110683
            String xml = IOHelper.getOMEXml( imp );
            if ( xml != null )
            {
                if ( ! IOHelper.checkMetadataConsistency( imp, xml ) )
                {
                    // Image dimensions do not equal metadata dimension; OME Metadata will thus not be saved.
                }
                else
                {
                    new File( uri, "OME" ).mkdirs();
                    String omeXmlPath = IOHelper.combinePath( uri, "OME", "METADATA.ome.xml" );
                    FileWriter writer = new FileWriter( omeXmlPath );
                    writer.write( xml );
                    writer.close();
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }

    }


    @NotNull
    private static String getN5ChunkSizeArg( int[] imageDimensionsXYCZT, int[] chunkDimensionsXYCZT )
    {
        ArrayList< String > nonSingletonChunkDimensions = new ArrayList<>();

        for ( int i = 0; i < chunkDimensionsXYCZT.length; i++ )
        {
            if ( imageDimensionsXYCZT[ i ] > 1 )
            {
                nonSingletonChunkDimensions.add( String.valueOf( chunkDimensionsXYCZT[ i ] ) );
            }
        }

        return Strings.join( ",", nonSingletonChunkDimensions );
    }
}
