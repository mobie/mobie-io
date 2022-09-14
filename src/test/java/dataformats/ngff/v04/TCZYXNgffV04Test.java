package dataformats.ngff.v04;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import dataformats.ngff.base.TCZYXNgffBaseTest;

@Slf4j
public class TCZYXNgffV04Test extends TCZYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tczyx.ome.zarr";
    public TCZYXNgffV04Test() throws SpimDataException {
        super(URL);
    }
}