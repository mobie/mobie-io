package projects.ngff.v03;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import projects.ngff.base.TCZYXNgffBaseTest;

@Slf4j
public class TCZYXNgffV03Test extends TCZYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.3/tczyx.ome.zarr";
    public TCZYXNgffV03Test() throws SpimDataException {
        super(URL);
    }
}