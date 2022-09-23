package dataformats.ngff.v04;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import dataformats.ngff.base.TYXNgffBaseTest;

@Slf4j
public class TYXNgffV04Test extends TYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tyx.ome.zarr";
    public TYXNgffV04Test() throws SpimDataException {
        super(URL);
    }
}