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
import net.imglib2.type.numeric.integer.UnsignedShortType;
import projects.remote.BaseTest;

@Slf4j
public abstract class YXNgffBaseTest extends BaseTest {
    private static final ImageDataFormat FORMAT = ImageDataFormat.OmeZarrS3;

    protected YXNgffBaseTest(String url) throws SpimDataException {
        super(url, FORMAT);
        //set values for base test
        setExpectedTimePoints(1);
        setExpectedShape(new FinalDimensions(1024, 930));
        setExpectedDType("uint16");
    }

    @Test
    public void checkDataset() {
        long x = 0;
        long y = 0;
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
        RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);

        UnsignedShortType o = (UnsignedShortType) randomAccessibleInterval.getAt(0, 0, 0);
        int value = o.get();
        int expectedValue = 538;
        Assertions.assertEquals(expectedValue, value);
        
        // random test data generated independently with python
        o = (UnsignedShortType) randomAccessibleInterval.getAt(294, 233, 0);
        value = o.get();
        expectedValue = 627;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedShortType) randomAccessibleInterval.getAt(970, 719, 0);
        value = o.get();
        expectedValue = 611;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedShortType) randomAccessibleInterval.getAt(962, 828, 0);
        value = o.get();
        expectedValue = 688;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedShortType) randomAccessibleInterval.getAt(219, 841, 0);
        value = o.get();
        expectedValue = 580;
        Assertions.assertEquals(expectedValue, value);
        
        o = (UnsignedShortType) randomAccessibleInterval.getAt(588, 710, 0);
        value = o.get();
        expectedValue = 594;
        Assertions.assertEquals(expectedValue, value);
    }
}
