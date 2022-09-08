package projects.ngff.v03;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import projects.ngff.base.TYXNgffBaseTest;

@Slf4j
public class TYXNgffV03Test extends TYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.3/tyx.ome.zarr";
    public TYXNgffV03Test() throws SpimDataException {
        super(URL);
    }
}