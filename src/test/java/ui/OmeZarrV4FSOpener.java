package ui;

import bdv.util.BdvFunctions;
import mpicbg.spim.data.SpimData;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrOpener;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;

import java.io.IOException;

public class OmeZarrV4FSOpener {
    public static void main(String[] args) throws IOException {
        showHela();
    }

    public static void showHela() throws IOException {
//        https://s3.embl.de/i2k-2020/ngff-example-data/v0.4
        OMEZarrOpener reader = new OMEZarrOpener(
                "/g/kreshuk/pape/Work/mobie/ngff/ome-ngff-prototypes/single_image/v0.4");
        OMEZarrS3Opener.setLogChunkLoading(true);
        SpimData image = OMEZarrOpener.openFile("/home/katerina/Documents/embl/mnt/kreshuk/kreshuk/pape/Work/mobie/ngff/ome-ngff-prototypes/single_image/v0.4/tcyx.ome.zarr");
        BdvFunctions.show(image);
    }
}
