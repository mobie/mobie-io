package dataformats.ngff.v04;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import dataformats.ngff.base.MultiImageNgffBaseTest;

@Slf4j
public class MultiImageNgffV04Test extends MultiImageNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/multi-image.ome.zarr";
    public MultiImageNgffV04Test() throws SpimDataException {
        super(URL);
    }
}