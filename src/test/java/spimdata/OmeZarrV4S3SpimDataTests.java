package spimdata;

import java.io.IOException;

import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.junit.jupiter.api.Test;

import mpicbg.spim.data.SpimData;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OmeZarrV4S3SpimDataTests {
    public static final String ZYX_FILE_KEY = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/zyx.ome.zarr";
    public static final String CZYX_FILE_KEY = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/czyx.ome.zarr";
    public static final String CYX_FILE_KEY = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/cyx.ome.zarr";

    @Test
    public void SpimDataV4UnitTest() throws IOException {
        SpimData spimData = OMEZarrS3Opener.readURL(ZYX_FILE_KEY);

        final String unit = spimData.getSequenceDescription().getViewSetupsOrdered().get(0).getVoxelSize().unit();
        final double[] dimensions = new double[3];
        spimData.getSequenceDescription().getViewSetupsOrdered().get(0).getVoxelSize().dimensions(dimensions);

        assertEquals("nanometer", unit);
        assertArrayEquals(dimensions, new double[]{64.0, 64.0, 64.0});
    }

    @Test
    public void SpimDataV4MultiChannelTestCZYX() throws IOException {
        System.out.println(CZYX_FILE_KEY);
        SpimData spimData = OMEZarrS3Opener.readURL(CZYX_FILE_KEY);
        //SpimData spimData = OMEZarrOpener.openFile( "/Users/tischer/Desktop/tischi-debug/data/Round1/images/ome-zarr/plate_01.ome.zarr/B/02/0" );


        // TODO: tricky: numTimepoints
        final int numSetups = spimData.getSequenceDescription().getViewSetupsOrdered().size();
        assertEquals(2, numSetups);

        for (int setupId = 0; setupId < numSetups; setupId++) {
            System.out.println("setup: " + setupId);
            Info info = new Info();
            info = info.getImgInfo(spimData, setupId);
            info.print();
            assertArrayEquals(new long[]{128, 66, 122}, info.dimensions);
            assertEquals(3, info.levels);
            if (setupId == 0)
                assertEquals(5115.0, info.max);
            else if (setupId == 1)
                assertEquals(280.0, info.max);
        }
    }
}
