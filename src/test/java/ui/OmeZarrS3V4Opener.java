package ui;

import java.io.IOException;

import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;

import bdv.util.BdvFunctions;
import mpicbg.spim.data.SpimData;
import net.imglib2.Dimensions;

public class OmeZarrS3V4Opener {
    public static void main(String[] args) throws IOException, InterruptedException {
        showCYX();
    }

    public static void showYX() throws IOException, InterruptedException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/yx.ome.zarr");
        BdvFunctions.show(image);
        Thread.sleep(10000);
    }

    public static void showZYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/zyx.ome.zarr");
        BdvFunctions.show(image);
    }

    public static void showCYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/cyx.ome.zarr");
        Dimensions dimensions = image.getSequenceDescription().getViewSetupsOrdered().get(0).getSize();
        System.out.println(image.getSequenceDescription().getViewSetupsOrdered().size());
        System.out.println(dimensions.toString());
        Dimensions dimensions1 = image.getSequenceDescription().getViewSetupsOrdered().get(1).getSize();
        System.out.println(dimensions1.toString());
        Dimensions dimensions2 = image.getSequenceDescription().getViewSetupsOrdered().get(2).getSize();
        System.out.println(dimensions2.toString());
        Dimensions dimensions3 = image.getSequenceDescription().getViewSetupsOrdered().get(3).getSize();
        System.out.println(dimensions3.toString());
        BdvFunctions.show(image);
    }

    public static void showTYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tyx.ome.zarr");
        Dimensions dimensions = image.getSequenceDescription().getViewSetupsOrdered().get(0).getSize();
        System.out.println(dimensions.toString());
        BdvFunctions.show(image);
    }

    public static void showTCYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tcyx.ome.zarr");
        Dimensions dimensions = image.getSequenceDescription().getViewSetupsOrdered().get(0).getSize();
        System.out.println(image.getSequenceDescription().getViewSetupsOrdered().size());
        System.out.println(dimensions.toString());
        Dimensions dimensions1 = image.getSequenceDescription().getViewSetupsOrdered().get(1).getSize();
        System.out.println(dimensions1.toString());
        BdvFunctions.show(image);
    }

    public static void showTCZYX() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/tczyx.ome.zarr");
        BdvFunctions.show(image);
    }

    public static void multiImg() throws IOException {
        SpimData image = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/multi-image.ome.zarr");
        BdvFunctions.show(image);
    }
}
