package spimdata;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimData;
import org.embl.mobie.io.openorganelle.OpenOrganelleS3Opener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@Slf4j
public class OpenOrganelleSpimDataTest {
    public static final OpenOrganelleS3Opener reader = new OpenOrganelleS3Opener(
            "https://janelia-cosem.s3.amazonaws.com",
            "us-west-2",
            "jrc_hela-2");
    public static final String FILE_KEY = "jrc_hela-2.n5/em/fibsem-uint16";
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
            SpimData spimData = reader.readKey(FILE_KEY);
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
            SpimData spimData = reader.readKey(FILE_KEY);
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
