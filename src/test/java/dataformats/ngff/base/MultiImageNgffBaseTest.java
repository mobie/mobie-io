package dataformats.ngff.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import bdv.img.cache.VolatileCachedCellImg;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedShortType;

@Slf4j
public abstract class MultiImageNgffBaseTest extends NgffBaseTest {
    public static final int MULTISCALES_SIZE = 4;

    protected MultiImageNgffBaseTest(String url) throws SpimDataException {
        super(url);
        //set values for base test
        setExpectedTimePoints(1);
        setExpectedShape(new FinalDimensions(1024, 930));
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
        long[] dims = new long[]{1024, 930, 1};
        int[] cellDims = new int[]{256, 256, 1};
        CellGrid expected = new CellGrid(dims, cellDims);
        Assertions.assertEquals(expected, cellGrid);
    }
    
    @Test
    public void checkImgValue() {

        // random test data generated independently with python
        RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);
        UnsignedShortType o = (UnsignedShortType) randomAccessibleInterval.getAt(847, 886, 0);
        int value = o.get();
        int expectedValue = 562;
        Assertions.assertEquals(expectedValue, value);
        
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(1).getImage(0);
        o = (UnsignedShortType) randomAccessibleInterval.getAt(265, 882, 0);
        value = o.get();
        expectedValue = 3328;
        Assertions.assertEquals(expectedValue, value);
        
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(2).getImage(0);
        o = (UnsignedShortType) randomAccessibleInterval.getAt(516, 621, 0);
        value = o.get();
        expectedValue = 2029;
        Assertions.assertEquals(expectedValue, value);
        
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(3).getImage(0);
        o = (UnsignedShortType) randomAccessibleInterval.getAt(874, 281, 0);
        value = o.get();
        expectedValue = 2325;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedShortType) randomAccessibleInterval.getAt(19, 602, 0);
        value = o.get();
        expectedValue = 2121;
        Assertions.assertEquals(expectedValue, value);
    }
    
    // make sure that we have 4 elements in the mulitscales represntation,
    // corresponding to the 4 different images stored on the multi-scales level
    @Test
    public void checkMultiscalesSize() {
        int multiscalesSize = spimData.getViewRegistrations().getViewRegistrations().size();
        Assertions.assertEquals(MULTISCALES_SIZE, multiscalesSize);
    }
}
