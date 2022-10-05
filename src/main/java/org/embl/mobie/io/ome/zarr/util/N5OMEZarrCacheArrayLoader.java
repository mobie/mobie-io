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
package org.embl.mobie.io.ome.zarr.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.embl.mobie.io.n5.util.N5DataTypeSize;
import org.embl.mobie.io.ome.zarr.loaders.N5OMEZarrImageLoader;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;

import com.amazonaws.SdkClientException;

import bdv.img.cache.SimpleCacheArrayLoader;
import lombok.extern.slf4j.Slf4j;
import net.imglib2.img.cell.CellGrid;

@Slf4j
public class N5OMEZarrCacheArrayLoader<A> implements SimpleCacheArrayLoader<A> {
    private final N5Reader n5;
    private final String pathName;
    private final int channel;
    private final int timepoint;
    private final DatasetAttributes attributes;
    private final ZarrArrayCreator<A, ?> zarrArrayCreator;
    private final ZarrAxes zarrAxes;

    public N5OMEZarrCacheArrayLoader(final N5Reader n5, final String pathName, final int channel, final int timepoint, final DatasetAttributes attributes, CellGrid grid, ZarrAxes zarrAxes) {
        this.n5 = n5;
        this.pathName = pathName; // includes the level
        this.channel = channel;
        this.timepoint = timepoint;
        this.attributes = attributes;
        final DataType dataType = attributes.getDataType();
        this.zarrArrayCreator = new ZarrArrayCreator<>(grid, dataType, zarrAxes);
        this.zarrAxes = zarrAxes;
    }

    @Override
    public A loadArray(final long[] gridPosition, int[] cellDimensions) throws IOException {
        DataBlock<?> block = null;

        long[] dataBlockIndices = toZarrChunkIndices(gridPosition);

        long start = 0;
        if (N5OMEZarrImageLoader.logging)
            start = System.currentTimeMillis();

        try {
            block = n5.readBlock(pathName, attributes, dataBlockIndices);
        } catch (SdkClientException e) {
            log.error(e.getMessage()); // this happens sometimes, not sure yet why...
        }
        if (N5OMEZarrImageLoader.logging) {
            if (block != null) {
                final long millis = System.currentTimeMillis() - start;
                final int numElements = block.getNumElements();
                final DataType dataType = attributes.getDataType();
                final float megaBytes = (float) numElements * N5DataTypeSize.getNumBytesPerElement(dataType) / 1000000.0F;
                final float mbPerSecond = megaBytes / (millis / 1000.0F);
                log.info(pathName + " " + Arrays.toString(dataBlockIndices) + ": " + "Read " + numElements + " " + dataType + " (" + String.format("%.3f", megaBytes) + " MB) in " + millis + " ms (" + String.format("%.3f", mbPerSecond) + " MB/s).");
            } else
                log.warn(pathName + " " + Arrays.toString(dataBlockIndices) + ": Missing, returning zeros.");
        }

        if (block == null) {
            return (A) zarrArrayCreator.createEmptyArray(gridPosition);
        } else {
            return zarrArrayCreator.createArray(block, gridPosition);
        }
    }

    private long[] toZarrChunkIndices(long[] gridPosition) {

        long[] chunkInZarr = new long[zarrAxes.getNumDimension()];

        // fill in the spatial dimensions
        final Map<Integer, Integer> spatialToZarr = zarrAxes.spatialToZarr();
        for (Map.Entry<Integer, Integer> entry : spatialToZarr.entrySet())
            chunkInZarr[entry.getValue()] = gridPosition[entry.getKey()];

        if (zarrAxes.hasChannels())
            chunkInZarr[zarrAxes.channelIndex()] = channel;

        if (zarrAxes.hasTimepoints())
            chunkInZarr[zarrAxes.timeIndex()] = timepoint;

        return chunkInZarr;
    }
}
