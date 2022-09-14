package dataformats.ngff.v04;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import dataformats.ngff.base.TCYXNgffBaseTest;

@Slf4j
public class TCYXNgffV04Test extends TCYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tcyx.ome.zarr";
    public TCYXNgffV04Test() throws SpimDataException {
        super(URL);
    }
}