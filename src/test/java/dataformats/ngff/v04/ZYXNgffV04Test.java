package dataformats.ngff.v04;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import dataformats.ngff.base.ZYXNgffBaseTest;

@Slf4j
public class ZYXNgffV04Test extends ZYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/zyx.ome.zarr";
    public ZYXNgffV04Test() throws SpimDataException {
        super(URL);
        setExpectedScale(new double[]{64.0, 64.0, 64.0});
        setExpectedUnit("nanometer");
    }
}