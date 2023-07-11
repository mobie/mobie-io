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
package org.embl.mobie.io.openorganelle;

import java.util.Arrays;

import net.imglib2.img.basictypeaccess.DataAccess;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;

import bdv.img.cache.SimpleCacheArrayLoader;
import lombok.extern.slf4j.Slf4j;
import net.imglib2.img.cell.CellGrid;

@Slf4j
public class N5CacheArrayLoader<A extends DataAccess> implements SimpleCacheArrayLoader<A> {
    private final N5Reader n5;
    private final String pathName;
    private final DatasetAttributes attributes;
    private final OrganelleArrayCreator<A, ?> arrayCreator;

    N5CacheArrayLoader(final N5Reader n5, final String pathName, final DatasetAttributes attributes, CellGrid grid) {
        this.n5 = n5;
        this.pathName = pathName;
        this.attributes = attributes;
        this.arrayCreator = new OrganelleArrayCreator<>(grid, attributes.getDataType());
    }

    @Override
    public A loadArray(final long[] gridPosition, int[] cellDimensions) {
        DataBlock<?> block = null;

        try {
            block = n5.readBlock(pathName, attributes, gridPosition);
        } catch (Exception e) {
            log.error("Error loading " + pathName + " at block " + Arrays.toString(gridPosition) + ": " + e);
        }

        if (block == null) {
            return (A) arrayCreator.createEmptyArray(gridPosition);
        } else {
            return arrayCreator.createArray(block, gridPosition);
        }
    }
}
