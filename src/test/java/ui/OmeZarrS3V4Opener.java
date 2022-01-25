package ui;

import bdv.util.BdvFunctions;
import mpicbg.spim.data.SpimData;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.embl.mobie.io.openorganelle.OpenOrganelleS3Opener;

import java.io.IOException;

public class OmeZarrS3V4Opener {
    public static void main(String[] args) throws IOException {
        showHela();
    }

    public static void showHela() throws IOException {
//        https://s3.embl.de/i2k-2020/ngff-example-data/v0.4
//        OMEZarrS3Opener reader = new OMEZarrS3Opener(
//                "https://s3.embl.de",
//                "us-west-2",
//                "i2k-2020");
//        OMEZarrS3Opener.setLogChunkLoading(true);
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/yx.ome.zarr");
        BdvFunctions.show(image);
    }
}
