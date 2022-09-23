package dataformats.ngff.base;

import org.embl.mobie.io.ImageDataFormat;
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
public abstract class CZYXNgffBaseTest extends NgffBaseTest {

    protected CZYXNgffBaseTest(String url) throws SpimDataException {
        super(url);
        //set values for base test
        setExpectedTimePoints(1);
        setExpectedChannelsNumber(2);
        setExpectedShape(new FinalDimensions(512, 262, 486, 2));
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
        long[] dims = new long[]{512, 262, 486};
        int[] cellDims = new int[]{64, 64, 64};
        CellGrid expected = new CellGrid(dims, cellDims);
        Assertions.assertEquals(expected, cellGrid);
    }
    
    @Test
    public void checkImgValue() {

        // random test data generated independently with python
        RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);
        ShortType o = (ShortType) randomAccessibleInterval.getAt(141, 27, 326);
        int value = o.get();
        int expectedValue = 6;
        Assertions.assertEquals(expectedValue, value);
        
        o = (ShortType) randomAccessibleInterval.getAt(120, 112, 326);
        value = o.get();
        expectedValue = 339;
        Assertions.assertEquals(expectedValue, value);
        
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(1).getImage(0);
        o = (ShortType) randomAccessibleInterval.getAt(70, 21, 303);
        value = o.get();
        expectedValue = 6;
        Assertions.assertEquals(expectedValue, value);
        
        o = (ShortType) randomAccessibleInterval.getAt(219, 253, 291);
        value = o.get();
        expectedValue = 5;
        Assertions.assertEquals(expectedValue, value);
        
        o = (ShortType) randomAccessibleInterval.getAt(355, 54, 251);
        value = o.get();
        expectedValue = 6;
        Assertions.assertEquals(expectedValue, value);
    }
}
