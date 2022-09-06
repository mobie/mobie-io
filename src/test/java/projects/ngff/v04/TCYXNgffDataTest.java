package projects.ngff.v04;

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
import net.imglib2.type.numeric.integer.ShortType;

@Slf4j
public class TCYXNgffDataTest extends BaseTest {
    private static final String URL= "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tcyx.ome.zarr";
    private static final ImageDataFormat FORMAT = ImageDataFormat.OmeZarrS3;

    public TCYXNgffDataTest() throws SpimDataException {
        super(URL, FORMAT);
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

        // random test data generated independently with python
        RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);
        ShortType o = (ShortType) randomAccessibleInterval.getAt(196, 136, 0);
        int value = o.get();
        int expectedValue = 495;
        Assertions.assertEquals(expectedValue, value);
        
        o = (ShortType) randomAccessibleInterval.getAt(367, 5, 0);
        value = o.get();
        expectedValue = 7;
        Assertions.assertEquals(expectedValue, value);
        
        o = (ShortType) randomAccessibleInterval.getAt(384, 90, 0);
        value = o.get();
        expectedValue = 33;
        Assertions.assertEquals(expectedValue, value);
        
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(1).getImage(0);
        o = (ShortType) randomAccessibleInterval.getAt(480, 196, 0);
        value = o.get();
        expectedValue = 7;
        Assertions.assertEquals(expectedValue, value);
        
        o = (ShortType) randomAccessibleInterval.getAt(37, 227, 0);
        value = o.get();
        expectedValue = 6;
        Assertions.assertEquals(expectedValue, value);
    }
}
