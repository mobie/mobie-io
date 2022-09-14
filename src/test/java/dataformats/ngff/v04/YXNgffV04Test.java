package dataformats.ngff.v04;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import dataformats.ngff.base.YXNgffBaseTest;

@Slf4j
public class YXNgffV04Test extends YXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/yx.ome.zarr";
    public YXNgffV04Test() throws SpimDataException {
        super(URL);
    }
}