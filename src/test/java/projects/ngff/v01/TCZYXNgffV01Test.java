package projects.ngff.v01;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import projects.ngff.base.TCZYXNgffBaseTest;

@Slf4j
public class TCZYXNgffV01Test extends TCZYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.1/tczyx.ome.zarr";
    public TCZYXNgffV01Test() throws SpimDataException {
        super(URL);
    }
}
