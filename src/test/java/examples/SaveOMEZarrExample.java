package examples;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import org.embl.mobie.io.OMEZarrWriter;

import java.io.File;


public class SaveOMEZarrExample
{
    public static void main( String[] args )
    {
        // Create image
        ImagePlus imp = IJ.createImage( "image", 500, 500, 500, 8 );
        Calibration calibration = new Calibration();
        calibration.setUnit( "micrometer" );
        calibration.pixelWidth = 0.2;
        calibration.pixelHeight = 0.2;
        calibration.pixelDepth = 1.0;
        imp.setCalibration( calibration );

        // Save image as OME-Zarr
        long start = System.currentTimeMillis();
        OMEZarrWriter.write( imp,
                new File("src/test/output/image.ome.zarr").getAbsolutePath(),
                OMEZarrWriter.ImageType.Intensities,
                true );
        System.out.println("Saving time [ms]: " + ( System.currentTimeMillis() - start ));
    }
}
