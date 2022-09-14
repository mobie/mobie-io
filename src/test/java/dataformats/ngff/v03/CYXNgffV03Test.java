package dataformats.ngff.v03;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import dataformats.ngff.base.CYXNgffBaseTest;

@Slf4j
public class CYXNgffV03Test extends CYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.3/cyx.ome.zarr";
    public CYXNgffV03Test() throws SpimDataException {
        super(URL);
    }
}