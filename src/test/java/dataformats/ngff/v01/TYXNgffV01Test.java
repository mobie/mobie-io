package dataformats.ngff.v01;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import dataformats.ngff.base.TYXNgffBaseTest;

@Slf4j
public class TYXNgffV01Test extends TYXNgffBaseTest{
    private static final String URL = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.1/tyx.ome.zarr";
    public TYXNgffV01Test() throws SpimDataException {
        super(URL);
        setExpectedShape(new FinalDimensions(512, 262, 1, 1, 3));
    }
}
