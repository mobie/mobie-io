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
package dataformats.ngff.base;

import mpicbg.spim.data.sequence.ImgLoader;
import mpicbg.spim.data.sequence.MultiResolutionImgLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import bdv.img.cache.VolatileCachedCellImg;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedShortType;


public abstract class CYXNgffBaseTest extends NgffBaseTest {
    protected CYXNgffBaseTest(String url) throws SpimDataException {
        super(url);
        //set values for base test
        setExpectedTimePoints(1);
        setExpectedChannelsNumber(4);
        setExpectedShape(new FinalDimensions(1024, 930, 4));
        setExpectedDType("uint16");
    }

    @Test
    public void checkDataset() {
        long x = 1;
        long y = 1;
        long z = 1;

        final int timepointId = 0;

        for ( int channelId = 0; channelId < expectedChannelsNumber; channelId++ )
        {
            long[] imageDimensions = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader( channelId ).getImage( timepointId ).dimensionsAsLongArray();
            if ( x > imageDimensions[ 0 ] || y > imageDimensions[ 1 ] || z > imageDimensions[ 2 ] )
            {
                throw new RuntimeException( "Coordinates out of bounds" );
            }

            RandomAccessibleInterval< ? > randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader( channelId ).getImage( timepointId );
            VolatileCachedCellImg volatileCachedCellImg = ( VolatileCachedCellImg ) randomAccessibleInterval;
            CellGrid cellGrid = volatileCachedCellImg.getCellGrid();
            long[] dims = new long[]{ 1024, 930, 1 };
            int[] cellDims = new int[]{ 256, 256, 1 };
            CellGrid expected = new CellGrid( dims, cellDims );
            Assertions.assertEquals( expected, cellGrid );
        }
    }
    
    @Test
    public void checkImgValue() {

        final int timepointId = 0;
        int channelId;
        int resolutionLevel;
        RandomAccessibleInterval<?> randomAccessibleInterval;
        UnsignedShortType o;
        int value;
        int expectedValue;


        final MultiResolutionImgLoader imgLoader = ( MultiResolutionImgLoader )  spimData.getSequenceDescription().getImgLoader();
        final int numResolutions = imgLoader.getSetupImgLoader( 0 ).getMipmapResolutions().length;

        // random test data generated independently with python

        // test high channel number in down-sampled resolution
        // this was where we thought that issue could be:
        // https://github.com/mobie/mobie-viewer-fiji/issues/945
        channelId = 3;
        resolutionLevel = 2;
        randomAccessibleInterval = imgLoader.getSetupImgLoader(channelId).getImage( timepointId, resolutionLevel );
        o = (UnsignedShortType) randomAccessibleInterval.getAt(770/4, 343/4, 0);
        value = o.get();
        //expectedValue = 2871;
        Assertions.assertTrue( value > 0 );

        channelId = 1;
        resolutionLevel = 0;
        randomAccessibleInterval = imgLoader.getSetupImgLoader( channelId ).getImage( timepointId, resolutionLevel );

        o = (UnsignedShortType) randomAccessibleInterval.getAt(647, 482, 0);
        value = o.get();
        expectedValue = 4055;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedShortType) randomAccessibleInterval.getAt(649, 346, 0);
        value = o.get();
        expectedValue = 4213;
        Assertions.assertEquals(expectedValue, value);

        channelId = 2;
        randomAccessibleInterval = imgLoader.getSetupImgLoader(channelId).getImage( timepointId, resolutionLevel );

        o = (UnsignedShortType) randomAccessibleInterval.getAt(559, 920, 0);
        value = o.get();
        expectedValue = 1835;
        Assertions.assertEquals(expectedValue, value);

        channelId = 3;
        randomAccessibleInterval = imgLoader.getSetupImgLoader(channelId).getImage( timepointId, resolutionLevel );

        o = (UnsignedShortType) randomAccessibleInterval.getAt(934, 929, 0);
        value = o.get();
        expectedValue = 1724;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedShortType) randomAccessibleInterval.getAt(770, 343, 0);
        value = o.get();
        expectedValue = 2871;
        Assertions.assertEquals(expectedValue, value);


    }
}
