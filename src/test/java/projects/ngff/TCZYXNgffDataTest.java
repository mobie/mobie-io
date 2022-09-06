package projects.ngff;

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

@Slf4j
public class TCZYXNgffDataTest extends BaseTest {
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tczyx.ome.zarr";
    private static final ImageDataFormat FORMAT = ImageDataFormat.OmeZarrS3;

    public TCZYXNgffDataTest() throws SpimDataException {
        super(URL, FORMAT);
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
}
