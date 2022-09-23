package dataformats.ngff.v03;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import dataformats.ngff.base.YXNgffBaseTest;

@Slf4j
public class YXNgffV03Test extends YXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.3/yx.ome.zarr";
    public YXNgffV03Test() throws SpimDataException {
        super(URL);
    }
}