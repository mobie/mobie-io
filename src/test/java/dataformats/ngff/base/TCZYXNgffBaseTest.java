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
public abstract class TCZYXNgffBaseTest extends NgffBaseTest {

    protected TCZYXNgffBaseTest(String url) throws SpimDataException {
        super(url);
        //set values for base test
        setExpectedTimePoints(3);
        setExpectedChannelsNumber(2);
        setExpectedShape(new FinalDimensions(512, 262, 486, 2, 3));
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
        ShortType o = (ShortType) randomAccessibleInterval.getAt(391, 70, 138);
        int value = o.get();
        int expectedValue = 7;
        Assertions.assertEquals(expectedValue, value);
        
        o = (ShortType) randomAccessibleInterval.getAt(91, 175, 178);
        value = o.get();
        expectedValue = 47;
        Assertions.assertEquals(expectedValue, value);
        
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(1).getImage(0);
        o = (ShortType) randomAccessibleInterval.getAt(458, 84, 65);
        value = o.get();
        expectedValue = 8;
        Assertions.assertEquals(expectedValue, value);
        
        o = (ShortType) randomAccessibleInterval.getAt(214, 105, 220);
        value = o.get();
        expectedValue = 37;
        Assertions.assertEquals(expectedValue, value);
        
        o = (ShortType) randomAccessibleInterval.getAt(207, 0, 99);
        value = o.get();
        expectedValue = 8;
        Assertions.assertEquals(expectedValue, value);
    }
}
