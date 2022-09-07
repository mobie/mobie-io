package projects.ngff.v02;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import projects.ngff.base.TCYXNgffBaseTest;

@Slf4j
public class TCYXNgffV02Test extends TCYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.2/tcyx.ome.zarr";
    public TCYXNgffV02Test() throws SpimDataException {
        super(URL);
        setExpectedShape(new FinalDimensions(512, 262, 1, 2, 3));
    }
}
