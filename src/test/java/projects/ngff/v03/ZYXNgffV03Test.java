package projects.ngff.v03;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import projects.ngff.base.ZYXNgffBaseTest;

@Slf4j
public class ZYXNgffV03Test extends ZYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.3/zyx.ome.zarr";
    public ZYXNgffV03Test() throws SpimDataException {
        super(URL);
    }
}