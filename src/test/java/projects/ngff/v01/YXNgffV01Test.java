package projects.ngff.v01;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import projects.ngff.base.YXNgffBaseTest;

@Slf4j
public class YXNgffV01Test extends YXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.1/yx.ome.zarr";
    public YXNgffV01Test() throws SpimDataException {
        super(URL);
        setExpectedShape(new FinalDimensions(1024, 930, 1, 1, 1));
    }
}
