package dataformats.ngff.v02;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import dataformats.ngff.base.CYXNgffBaseTest;

@Slf4j
public class CYXNgffV02Test extends CYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.2/cyx.ome.zarr";
    public CYXNgffV02Test() throws SpimDataException {
        super(URL);
        setExpectedShape(new FinalDimensions(1024, 930, 1, 4, 1));
    }
}
