package dataformats.ngff.base;

import org.embl.mobie.io.ImageDataFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mpicbg.spim.data.SpimDataException;

import lombok.extern.slf4j.Slf4j;
import dataformats.BaseTest;

@Slf4j
public abstract class NgffBaseTest extends BaseTest {
    private static final ImageDataFormat FORMAT = ImageDataFormat.OmeZarrS3;
    protected float[] expectedScale = null;
    
    protected NgffBaseTest(String url) throws SpimDataException {
        super(url, FORMAT);
    }
    
    @Test
    public void baseTest() {
        super.baseTest();
        if(expectedScale != null) {
            Assertions.assertEquals(expectedScale, getScale());
        }
    }

    // TODO read the scale info
    private float[] getScale() {
        return new float[3];
    }

}
