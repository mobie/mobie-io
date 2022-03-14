package spimdata;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimData;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@Slf4j
public class OmeZarrS3SpimDataTests {
    public static final String ZYX_FILE_KEY = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.3/zyx.ome.zarr";
    public static final int N = 3;
    public final Map<long[], Object> trueValuesMap = new LinkedHashMap<>();

    public int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    @BeforeEach
    public void init() {
        log.info("Before init() method called");
        try {
            OMEZarrS3Opener.setLogging( true );
            SpimData spimData = OMEZarrS3Opener.readURL(ZYX_FILE_KEY);
            long[] imageDimensions = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).dimensionsAsLongArray();
            for (int i = 0; i <= N; i++) {
                long x = getRandomNumberUsingNextInt(0, Math.toIntExact(imageDimensions[0] - 1));
                long y = getRandomNumberUsingNextInt(0, Math.toIntExact(imageDimensions[1] - 1));
                long z = getRandomNumberUsingNextInt(0, Math.toIntExact(imageDimensions[2] - 1));
                long[] axes = new long[]{x, y, z};
                Object realPixelValue = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).getAt(x, y, z);
                trueValuesMap.put(axes, realPixelValue);
            }
        } catch (IOException e) {
            Assertions.fail("SpimData loading error: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Random SpimData test")
    public void RandomSpimDataTest() {
        log.info("Running random test");
        try {
            OMEZarrS3Opener.setLogging(true);
            SpimData spimData = OMEZarrS3Opener.readURL(ZYX_FILE_KEY);
            List<Object> testValues = new ArrayList<>();
            for (long[] axes : trueValuesMap.keySet()) {
                Object realPixelValue = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).getAt(axes[0], axes[1], axes[2]);
                testValues.add(realPixelValue);
            }
            assertArrayEquals(testValues.toArray(), trueValuesMap.values().toArray());
        } catch (IOException e) {
            Assertions.fail("SpimData loading error: " + e.getMessage());
        }
    }
}
