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

import bdv.export.ExportMipmapInfo;
import bdv.export.ProposeMipmaps;
import bdv.viewer.Source;
import ij.ImagePlus;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import org.embl.mobie.io.util.IOHelper;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.ij.N5Importer;
import org.janelia.saalfeldlab.n5.ij.N5ScalePyramidExporter;

import java.net.URISyntaxException;

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
        N5ScalePyramidExporter.DOWNSAMPLE_METHOD downsampleMethod =
                imageType.equals( ImageType.Labels ) ?
                        N5ScalePyramidExporter.DOWNSAMPLE_METHOD.Sample
                        : N5ScalePyramidExporter.DOWNSAMPLE_METHOD.Average;

        String chunkSizeArg = imp.getNSlices() == 1 ?  "1024,1024,1,1,1" : "96,96,1,96,1"; // X,Y,C,Z,T


        try
        {
            N5URI n5URI = new N5URI( uri );
            String containerPath = n5URI.getContainerPath();
            String groupPath = n5URI.getGroupPath();
            int a = 1;

            N5ScalePyramidExporter exporter = new N5ScalePyramidExporter(
                    imp,
                    containerPath,
                    groupPath,
                    ZARR_FORMAT,
                    chunkSizeArg,
                    true,
                    downsampleMethod,
                    N5Importer.MetadataOmeZarrKey,
                    GZIP_COMPRESSION
            );

            exporter.setOverwrite( overwrite );
            exporter.run();
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( e );
        }

        // TODO: If we want to give the dataset a name we also have to
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

}
