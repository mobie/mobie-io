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
import net.imglib2.type.numeric.integer.UnsignedShortType;
import projects.remote.BaseTest;

@Slf4j
public class MultiImageNgffDataTest extends BaseTest {
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/multi-image.ome.zarr";
    private static final ImageDataFormat FORMAT = ImageDataFormat.OmeZarrS3;

    public MultiImageNgffDataTest() throws SpimDataException {
        super(URL, FORMAT);
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

		// Image-0 : (847, 886) : 562
        // random test data generated independently with python
        RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);
        UnsignedShortType o = (UnsignedShortType) randomAccessibleInterval.getAt(847, 886, 0);
        int value = o.get();
        int expectedValue = 562;
        Assertions.assertEquals(expectedValue, value);
        
		// Image-1 : (265, 882) : 3328
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(1).getImage(0);
        o = (UnsignedShortType) randomAccessibleInterval.getAt(265, 882, 0);
        value = o.get();
        expectedValue = 3328;
        Assertions.assertEquals(expectedValue, value);
        
		// Image-2 : (516, 621) : 2029
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(2).getImage(0);
        o = (UnsignedShortType) randomAccessibleInterval.getAt(516, 621, 0);
        value = o.get();
        expectedValue = 2029;
        Assertions.assertEquals(expectedValue, value);
        
		// Image-3 : (874, 281) : 2325
        randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(3).getImage(0);
        o = (UnsignedShortType) randomAccessibleInterval.getAt(874, 281, 0);
        value = o.get();
        expectedValue = 2325;
        Assertions.assertEquals(expectedValue, value);
        
		// Image-3 : (19, 602) : 2121
        o = (UnsignedShortType) randomAccessibleInterval.getAt(19, 602, 0);
        value = o.get();
        expectedValue = 2121;
        Assertions.assertEquals(expectedValue, value);
    }
}
