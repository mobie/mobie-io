package ui;

import bdv.util.BdvFunctions;
import mpicbg.spim.data.SpimData;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class OmeZarrS3V4Opener {

    @Test
    public void showYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/yx.ome.zarr");
        BdvFunctions.show(image);
    }

    @Test
    public void showZYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/zyx.ome.zarr");
        BdvFunctions.show(image);
    }

    @Test
    public void showCYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/cyx.ome.zarr");
        BdvFunctions.show(image);
    }

    @Test
    public void showTYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tyx.ome.zarr");
        BdvFunctions.show(image);
    }

    @Test
    public void showTCYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tcyx.ome.zarr");
        BdvFunctions.show(image);
    }

    @Test
    public void showTCZYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tczyx.ome.zarr");
        BdvFunctions.show(image);
    }

    @Test
    public void multiImg() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/multi-image.ome.zarr");
        BdvFunctions.show(image);
    }
}
