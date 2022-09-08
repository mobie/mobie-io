package projects.ngff.v04;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import projects.ngff.base.CZYXNgffBaseTest;

@Slf4j
public class CZYXNgffV04Test extends CZYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/czyx.ome.zarr";
    public CZYXNgffV04Test() throws SpimDataException {
        super(URL);
    }
}