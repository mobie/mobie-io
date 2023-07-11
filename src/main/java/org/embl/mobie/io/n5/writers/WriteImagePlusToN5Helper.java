/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2023 EMBL
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
package org.embl.mobie.io.n5.writers;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.realtransform.AffineTransform3D;

public class WriteImagePlusToN5Helper {
    public static boolean isImageSuitable(ImagePlus imp) {
        // check the image type
        switch (imp.getType()) {
            case ImagePlus.GRAY8:
            case ImagePlus.GRAY16:
            case ImagePlus.GRAY32:
                break;
            default:
                IJ.showMessage("Only 8, 16, 32-bit images are supported currently!");
                return false;
        }

        // check the image dimensionality
        if (imp.getNDimensions() < 2) {
            IJ.showMessage("Image must be at least 2-dimensional!");
            return false;
        }

        return true;
    }

    public static FinalVoxelDimensions getVoxelSize(ImagePlus imp) {
        final double pw = imp.getCalibration().pixelWidth;
        final double ph = imp.getCalibration().pixelHeight;
        final double pd = imp.getCalibration().pixelDepth;
        String punit = imp.getCalibration().getUnit();
        if (punit == null || punit.isEmpty())
            punit = "px";
        final FinalVoxelDimensions voxelSize = new FinalVoxelDimensions(punit, pw, ph, pd);
        return voxelSize;
    }

    public static FinalDimensions getSize(ImagePlus imp) {
        final int w = imp.getWidth();
        final int h = imp.getHeight();
        final int d = imp.getNSlices();
        final FinalDimensions size = new FinalDimensions(w, h, d);
        return size;
    }

    public static File getSeqFileFromPath(String seqFilename) {
        final File seqFile = new File(seqFilename);
        final File parent = seqFile.getParentFile();
        if (parent == null || !parent.exists() || !parent.isDirectory()) {
            IJ.showMessage("Invalid export filename " + seqFilename);
            return null;
        }
        return seqFile;
    }

    public static AffineTransform3D generateSourceTransform(FinalVoxelDimensions voxelSize) {
        // create SourceTransform from the images calibration
        final AffineTransform3D sourceTransform = new AffineTransform3D();
        sourceTransform.set(voxelSize.dimension(0), 0, 0, 0, 0, voxelSize.dimension(1),
            0, 0, 0, 0, voxelSize.dimension(2), 0);
        return sourceTransform;
    }

    public static File getN5FileFromXmlPath(String xmlPath) {
        final String n5Filename = xmlPath.substring(0, xmlPath.length() - 4) + ".n5";
        return new File(n5Filename);
    }

    public static File getOmeZarrFileFromXmlPath(String xmlPath) {
        final String omeZarrFileName = xmlPath.substring(0, xmlPath.length() - 4) + ".ome.zarr";
        return new File(omeZarrFileName);
    }
}
