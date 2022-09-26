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
package org.embl.mobie.io.ome.zarr.writers.imageplus;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.embl.mobie.io.n5.util.DownsampleBlock;
import org.embl.mobie.io.n5.util.ExportScalePyramid;
import org.embl.mobie.io.n5.writers.WriteImagePlusToN5;
import org.janelia.saalfeldlab.n5.Compression;

import bdv.export.ExportMipmapInfo;
import bdv.export.ProgressWriter;
import bdv.export.ProposeMipmaps;
import bdv.export.SubTaskProgressWriter;
import bdv.spimdata.SequenceDescriptionMinimal;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.realtransform.AffineTransform3D;

import static org.embl.mobie.io.n5.writers.WriteImagePlusToN5Helper.getSize;
import static org.embl.mobie.io.n5.writers.WriteImagePlusToN5Helper.getVoxelSize;

public class WriteImagePlusToN5OmeZarr extends WriteImagePlusToN5 {

    // TODO - deal with transforms properly - here the voxel size is just taken directly from the imp for scaling.
    // the sourceTransform is ignored. In next ome-zarr version, when affine is supported,
    // need to properly add the provided affine.
    // Also, I think viewSetupNames are ignored, as they are only relevant for xml style files. Should re-write this
    // so less redundant now that the bdv xml style is removed

    // export, generating default source transform, and default resolutions / subdivisions
    @Override
    public void export(ImagePlus imp, String zarrPath, DownsampleBlock.DownsamplingMethod downsamplingMethod,
                       Compression compression) {
        super.export(imp, zarrPath, downsamplingMethod, compression);
    }

    // export, generating default resolutions / subdivisions
    @Override
    public void export(ImagePlus imp, String zarrPath, AffineTransform3D sourceTransform,
                       DownsampleBlock.DownsamplingMethod downsamplingMethod, Compression compression) {
        super.export(imp, zarrPath, sourceTransform, downsamplingMethod, compression);
    }


    // export, generating default resolutions / subdivisions
    @Override
    public void export(ImagePlus imp, String zarrPath, AffineTransform3D sourceTransform,
                       DownsampleBlock.DownsamplingMethod downsamplingMethod, Compression compression,
                       String[] viewSetupNames) {
        super.export(imp, zarrPath, sourceTransform, downsamplingMethod, compression, viewSetupNames);
    }

    @Override
    public void export(ImagePlus imp, int[][] resolutions, int[][] subdivisions, String zarrPath,
                       AffineTransform3D sourceTransform, DownsampleBlock.DownsamplingMethod downsamplingMethod,
                       Compression compression) {
        export(imp, resolutions, subdivisions, zarrPath, sourceTransform, downsamplingMethod, compression, null);
    }

    @Override
    public void export(ImagePlus imp, int[][] resolutions, int[][] subdivisions, String zarrPath,
                       AffineTransform3D sourceTransform, DownsampleBlock.DownsamplingMethod downsamplingMethod,
                       Compression compression, String[] viewSetupNames) {
        if (resolutions.length == 0) {
            IJ.showMessage("Invalid resolutions - length 0");
            return;
        }

        if (subdivisions.length == 0) {
            IJ.showMessage(" Invalid subdivisions - length 0");
            return;
        }

        if (resolutions.length != subdivisions.length) {
            IJ.showMessage("Subsampling factors and chunk sizes must have the same number of elements");
            return;
        }

        final File zarrFile = new File(zarrPath);

        Parameters exportParameters = new Parameters(resolutions, subdivisions, null, zarrFile, sourceTransform,
            downsamplingMethod, compression, viewSetupNames, imp.getCalibration().getTimeUnit(),
            imp.getCalibration().frameInterval);

        export(imp, exportParameters);
    }

    @Override
    protected Parameters generateDefaultParameters(ImagePlus imp, String zarrPath, AffineTransform3D sourceTransform,
                                                   DownsampleBlock.DownsamplingMethod downsamplingMethod, Compression compression,
                                                   String[] viewSetupNames) {
        FinalVoxelDimensions voxelSize = getVoxelSize(imp);
        FinalDimensions size = getSize(imp);

        // propose reasonable mipmap settings
        final int maxNumElements = 64 * 64 * 64;
        final ExportMipmapInfo autoMipmapSettings = ProposeMipmaps.proposeMipmaps(
            new BasicViewSetup(0, "", size, voxelSize),
            maxNumElements);

        int[][] resolutions = autoMipmapSettings.getExportResolutions();
        int[][] subdivisions = autoMipmapSettings.getSubdivisions();

        if (resolutions.length == 0 || subdivisions.length == 0 || resolutions.length != subdivisions.length) {
            IJ.showMessage("Error with calculating default subdivisions and resolutions");
            return null;
        }

        final File zarrFile = new File(zarrPath);

        return new Parameters(resolutions, subdivisions, null, zarrFile, sourceTransform,
            downsamplingMethod, compression, viewSetupNames, imp.getCalibration().getTimeUnit(),
            imp.getCalibration().frameInterval);
    }

    @Override
    protected void writeFiles(SequenceDescriptionMinimal seq, Map<Integer, ExportMipmapInfo> perSetupExportMipmapInfo,
                              Parameters params, ExportScalePyramid.LoopbackHeuristic loopbackHeuristic,
                              ExportScalePyramid.AfterEachPlane afterEachPlane, int numCellCreatorThreads,
                              ProgressWriter progressWriter, int numTimepoints, int numSetups) throws IOException {
        WriteSequenceToN5OmeZarr.writeOmeZarrFile(seq, perSetupExportMipmapInfo,
            params.downsamplingMethod,
            params.compression, params.timeUnit, params.frameInterval, params.n5File,
            loopbackHeuristic, afterEachPlane, numCellCreatorThreads,
            new SubTaskProgressWriter(progressWriter, 0, 0.95));

        progressWriter.setProgress(1.0);
    }
}
