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
package org.embl.mobie.io.openorganelle;

import java.util.Arrays;

import org.embl.mobie.io.n5.util.ArrayCreator;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DataType;

import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;

public class OrganelleArrayCreator<A, T extends NativeType<T>> extends ArrayCreator {
    public OrganelleArrayCreator(CellGrid cellGrid, DataType dataType) {
        super(cellGrid, dataType);
    }

    public A createArray(DataBlock<?> dataBlock, long[] gridPosition) {
        long[] cellDims = getCellDims(gridPosition);
        int n = (int) (cellDims[0] * cellDims[1] * cellDims[2]);
        return (A) VolatileDoubleArray(dataBlock, cellDims, n);
    }

    @Override
    public long[] getCellDims(long[] gridPosition) {
        long[] cellMin = new long[3];
        int[] cellDims = new int[3];
        cellGrid.getCellDimensions(gridPosition, cellMin, cellDims);
        return Arrays.stream(cellDims).mapToLong(i -> i).toArray(); // casting to long for creating ArrayImgs.*
    }
}
