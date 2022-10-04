package dataformats.ngff.v02;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import dataformats.ngff.base.ZYXNgffBaseTest;

@Slf4j
public class ZYXNgffV02Test extends ZYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.2/zyx.ome.zarr";
    public ZYXNgffV02Test() throws SpimDataException {
        super(URL);
        setExpectedShape(new FinalDimensions(483, 393, 603, 1, 1));
    }
}
