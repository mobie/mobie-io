package dataformats.ngff.base;

import org.embl.mobie.io.ImageDataFormat;

import mpicbg.spim.data.SpimDataException;

import lombok.extern.slf4j.Slf4j;
import dataformats.BaseTest;

@Slf4j
public abstract class NgffBaseTest extends BaseTest {
    private static final ImageDataFormat FORMAT = ImageDataFormat.OmeZarrS3;
    
    protected NgffBaseTest(String url) throws SpimDataException {
        super(url, FORMAT);
    }

}
