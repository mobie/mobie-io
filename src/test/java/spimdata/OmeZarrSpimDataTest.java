package spimdata;

import de.embl.cba.n5.ome.zarr.openers.OMEZarrOpener;
import mpicbg.spim.data.SpimData;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class OmeZarrSpimDataTest {
    private static final String MAGICAL_PIXEL_VALUE = "131";

    @Test
    public void SpimDataTest() {
        try {
            SpimData spimData = OMEZarrOpener.openFile("/home/katerina/Documents/data/v0.3/zyx.ome.zarr");
            Object realPixelValue = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).getAt(225, 129, 301);
            assertEquals(realPixelValue.toString(), MAGICAL_PIXEL_VALUE);
        } catch (IOException e) {
            fail("SpimData loading error: " + e.getMessage());
        }
    }
}
