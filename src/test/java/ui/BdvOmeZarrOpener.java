package ui;

import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;

import bdv.util.BdvFunctions;
import bdv.util.volatiles.SharedQueue;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;

public class BdvOmeZarrOpener {
    public static void main(String[] args) {
        showProject();
    }

    public static void showProject() {
        SharedQueue sharedQueue = new SharedQueue(7);
        SpimDataOpener spimDataOpener = new SpimDataOpener();
        SpimData image = null;
        try {
//            image =(SpimData) spimDataOpener.openSpimData("https://raw.githubusercontent.com/mobie/clem-example-project//more-views/data/hela/images/bdv-n5-s3/fluorescence-a2-FMR-c2.xml",
//                    ImageDataFormat.BdvN5S3, sharedQueue);
            image = (SpimData) spimDataOpener.openSpimData("https://s3.embl.de/i2k-2020/project-bdv-ome-zarr/Covid19-S4-Area2/images/bdv.ome.zarr.s3/raw.xml",
                ImageDataFormat.BdvOmeZarrS3, sharedQueue);
        } catch (SpimDataException e) {
            e.printStackTrace();
        }
        assert image != null;
        BdvFunctions.show(image);
    }
}
