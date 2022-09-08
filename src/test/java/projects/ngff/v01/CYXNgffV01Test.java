package projects.ngff.v01;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import projects.ngff.base.CYXNgffBaseTest;

@Slf4j
public class CYXNgffV01Test extends CYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.1/cyx.ome.zarr";
    public CYXNgffV01Test() throws SpimDataException {
        super(URL);
        setExpectedShape(new FinalDimensions(1024, 930, 1, 4, 1));
    }
}
