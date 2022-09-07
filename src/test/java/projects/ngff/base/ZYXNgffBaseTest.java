package projects.ngff.base;

import org.embl.mobie.io.ImageDataFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import bdv.img.cache.VolatileCachedCellImg;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import projects.remote.BaseTest;
import net.imglib2.type.numeric.integer.UnsignedByteType;

@Slf4j
public abstract class ZYXNgffBaseTest extends BaseTest {
    private static final ImageDataFormat FORMAT = ImageDataFormat.OmeZarrS3;

    protected ZYXNgffBaseTest(String url) throws SpimDataException {
        super(url, FORMAT);
        //set values for base test
        setExpectedTimePoints(1);
        setExpectedShape(new FinalDimensions(483, 393, 603));
        setExpectedDType("uint8");
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
        long[] dims = new long[]{483, 393, 603};
        int[] cellDims = new int[]{64, 64, 64};
        CellGrid expected = new CellGrid(dims, cellDims);
        Assertions.assertEquals(expected, cellGrid);
    }
    
    @Test
    public void checkImgValue() {
        RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);

        // random test data generated independently with python
        UnsignedByteType o = (UnsignedByteType) randomAccessibleInterval.getAt(232, 73, 503);
        int value = o.get();
        int expectedValue = 137;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedByteType) randomAccessibleInterval.getAt(139, 180, 136);
        value = o.get();
        expectedValue = 104;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedByteType) randomAccessibleInterval.getAt(165, 37, 581);
        value = o.get();
        expectedValue = 156;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedByteType) randomAccessibleInterval.getAt(399, 45, 594);
        value = o.get();
        expectedValue = 138;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedByteType) randomAccessibleInterval.getAt(116, 381, 281);
        value = o.get();
        expectedValue = 156;
        Assertions.assertEquals(expectedValue, value);
    }
}
