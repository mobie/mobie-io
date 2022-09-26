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
package dataformats.ngff.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import bdv.img.cache.VolatileCachedCellImg;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.ShortType;

@Slf4j
public abstract class TCYXNgffBaseTest extends NgffBaseTest {

    protected TCYXNgffBaseTest(String url) throws SpimDataException {
        super(url);
        //set values for base test
        setExpectedTimePoints(3);
        setExpectedChannelsNumber(2);
        setExpectedShape(new FinalDimensions(512, 262, 2, 3));
        setExpectedDType("int16");
    }

    @Test
    public void checkDataset() {
        long x = 1;
        long y = 1;
        long z = 1;
        long[] imageDimensions = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).dimensionsAsLongArray();
        if (x > imageDimensions[0] || y > imageDimensions[1] || z > imageDimensions[2]) {
            throw new RuntimeException("Coordinates out of bounds");
        }

        RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);
        VolatileCachedCellImg volatileCachedCellImg = (VolatileCachedCellImg) randomAccessibleInterval;
        CellGrid cellGrid = volatileCachedCellImg.getCellGrid();
        long[] dims = new long[]{512, 262, 1};
        int[] cellDims = new int[]{256, 256, 1};
        CellGrid expected = new CellGrid(dims, cellDims);
        Assertions.assertEquals(expected, cellGrid);
    }

    @Test
    public void checkImgValue() {

        // random test data generated independently with python (coordinates are givn as tcxy)
        // channel 0, tp 0
        // (0, 0, 508, 200) : 82
        RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);
        ShortType o = (ShortType) randomAccessibleInterval.getAt(508, 200, 0);
        int value = o.get();
        int expectedValue = 82;
        Assertions.assertEquals(expectedValue, value);
        
        // (0, 0, 84, 255) : 8
        o = (ShortType) randomAccessibleInterval.getAt(84, 255, 0);
        value = o.get();
        expectedValue = 8;
        Assertions.assertEquals(expectedValue, value);
        
        // (0, 0, 386, 168) : 228
        o = (ShortType) randomAccessibleInterval.getAt(386, 168, 0);
        value = o.get();
        expectedValue = 228;
        Assertions.assertEquals(expectedValue, value);
        
        // channel 0, tp 1
        // (1, 0, 380, 118) : 21
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(1);
        o = (ShortType) randomAccessibleInterval.getAt(380, 118, 0);
        value = o.get();
        expectedValue = 21;
        Assertions.assertEquals(expectedValue, value);
        
        // channel 1, tp 2
        // (2, 1, 243, 255) : 7
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(1).getImage(2);
        o = (ShortType) randomAccessibleInterval.getAt(243, 255, 0);
        value = o.get();
        expectedValue = 7;
        Assertions.assertEquals(expectedValue, value);
    }
}
