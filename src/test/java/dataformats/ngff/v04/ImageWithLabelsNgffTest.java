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
package dataformats.ngff.v04;

import org.embl.mobie.io.ImageDataFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import bdv.img.cache.VolatileCachedCellImg;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import dataformats.BaseTest;

/*
 * Test for image data with labels, to ensure that this data can be read correctly
*/


public class ImageWithLabelsNgffTest extends BaseTest {
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/image-with-labels.ome.zarr";
    private static final ImageDataFormat FORMAT = ImageDataFormat.OmeZarrS3;

    public ImageWithLabelsNgffTest() throws SpimDataException {
        super(URL, FORMAT);
        //set values for base test
        setExpectedTimePoints(1);
        setExpectedChannelsNumber(4);
        setExpectedShape(new FinalDimensions(2048, 2048, 6, 4));
        setExpectedDType("uint16");
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
        long[] dims = new long[]{2048, 2048, 6};
        int[] cellDims = new int[]{512, 512, 1};
        CellGrid expected = new CellGrid(dims, cellDims);
        Assertions.assertEquals(expected, cellGrid);
    }
    
    @Test
    public void checkImgValue() {

        // random test data generated independently with python
        //(0, 992, 397, 4) : 170
        RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);
        // ShortType o = (ShortType) randomAccessibleInterval.getAt(992, 397, 4);
        UnsignedShortType o = (UnsignedShortType) randomAccessibleInterval.getAt(992, 397, 4);
        int value = o.get();
        int expectedValue = 170;
        Assertions.assertEquals(expectedValue, value);
        
        //(1, 92, 762, 5) : 163
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(1).getImage(0);
        // o = (ShortType) randomAccessibleInterval.getAt(92, 762, 5);
        o = (UnsignedShortType) randomAccessibleInterval.getAt(92, 762, 5);
        value = o.get();
        expectedValue = 163;
        Assertions.assertEquals(expectedValue, value);
        
        //(1, 405, 1294, 4) : 186
        // o = (ShortType) randomAccessibleInterval.getAt(405, 1294, 4);
        o = (UnsignedShortType) randomAccessibleInterval.getAt(405, 1294, 4);
        value = o.get();
        expectedValue = 186;
        Assertions.assertEquals(expectedValue, value);
        
        //(1, 737, 900, 3) : 177
        // o = (ShortType) randomAccessibleInterval.getAt(737, 900, 3);
        o = (UnsignedShortType) randomAccessibleInterval.getAt(737, 900, 3);
        value = o.get();
        expectedValue = 177;
        Assertions.assertEquals(expectedValue, value);
        
        //(3, 520, 269, 3) : 1143
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(3).getImage(0);
        // o = (ShortType) randomAccessibleInterval.getAt(520, 269, 3);
        o = (UnsignedShortType) randomAccessibleInterval.getAt(520, 269, 3);
        value = o.get();
        expectedValue = 1143;
        Assertions.assertEquals(expectedValue, value);
    }
}
