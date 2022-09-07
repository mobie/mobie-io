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
public class TYXNgffDataTest extends BaseTest {
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tyx.ome.zarr";
    private static final ImageDataFormat FORMAT = ImageDataFormat.OmeZarrS3;

    public TYXNgffDataTest() throws SpimDataException {
        super(URL, FORMAT);
        //set values for base test
        setExpectedTimePoints(3);
        setExpectedShape(new FinalDimensions(512, 262, 3));
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

        // random test data generated independently with python (coordinates = txy)
        // timepoint 0
        //(0, 183, 238) : 8
        RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);
        ShortType o = (ShortType) randomAccessibleInterval.getAt(183, 238, 0);
        int value = o.get();
        int expectedValue = 8;
        Assertions.assertEquals(expectedValue, value);
        
        // timepoint 1
        //(1, 325, 207) : 32
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(1);
        o = (ShortType) randomAccessibleInterval.getAt(325, 207, 0);
        value = o.get();
        expectedValue = 32;
        Assertions.assertEquals(expectedValue, value);
        
        //(1, 409, 175) : 133
        o = (ShortType) randomAccessibleInterval.getAt(409, 175, 0);
        value = o.get();
        expectedValue = 133;
        Assertions.assertEquals(expectedValue, value);
        
        //(1, 109, 144) : 415
        o = (ShortType) randomAccessibleInterval.getAt(109, 144, 0);
        value = o.get();
        expectedValue = 415;
        Assertions.assertEquals(expectedValue, value);
        
        // timepoint 2
        //(2, 447, 132) : 64
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(2);
        o = (ShortType) randomAccessibleInterval.getAt(447, 132, 0);
        value = o.get();
        expectedValue = 64;
        Assertions.assertEquals(expectedValue, value);
    }
}
