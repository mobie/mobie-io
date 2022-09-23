package dataformats.ngff.v03;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import dataformats.ngff.base.TCYXNgffBaseTest;

@Slf4j
public class TCYXNgffV03Test extends TCYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.3/tcyx.ome.zarr";
    public TCYXNgffV03Test() throws SpimDataException {
        super(URL);
    }
}