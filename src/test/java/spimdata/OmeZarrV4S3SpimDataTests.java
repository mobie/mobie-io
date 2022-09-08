package spimdata;

import mpicbg.spim.data.SpimData;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class OmeZarrV4S3SpimDataTests {
    public static final String ZYX_FILE_KEY = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/zyx.ome.zarr";

    @Test
    public void SpimDataV4UnitTest() throws IOException {
        SpimData spimData = OMEZarrS3Opener.readURL(ZYX_FILE_KEY);

        final String unit = spimData.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize().unit();
        final double[] dimensions = new double[ 3 ];
        spimData.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize().dimensions( dimensions );

        assertEquals("nanometer", unit);
        assertArrayEquals( dimensions, new double[]{64.0, 64.0, 64.0});
    }
}
