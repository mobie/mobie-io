package ui;

import java.io.IOException;

import org.embl.mobie.io.openorganelle.OpenOrganelleS3Opener;

import bdv.util.BdvFunctions;
import mpicbg.spim.data.SpimData;

public class OpenOrganelleHelaTest {

    public static void main(String[] args) throws IOException {
        showHela();
    }

    public static void showHela() throws IOException {
        OpenOrganelleS3Opener reader = new OpenOrganelleS3Opener(
            "https://janelia-cosem.s3.amazonaws.com",
            "us-west-2",
            "jrc_hela-2");
        OpenOrganelleS3Opener.setLogging(true);
        SpimData image = reader.readKey("jrc_hela-2.n5/em/fibsem-uint16");
        BdvFunctions.show(image);
    }
}
