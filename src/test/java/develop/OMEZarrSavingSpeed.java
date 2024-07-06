package develop;

import ij.IJ;
import ij.ImagePlus;
import org.embl.mobie.io.OMEZarrWriter;

public class OMEZarrSavingSpeed
{
    public static void main( String[] args )
    {
        int size = 100; // => 2 s
        ImagePlus imp = IJ.createImage( "image", size, size, size, 8 );

        long start = System.currentTimeMillis();
        OMEZarrWriter.write( imp,
                "/Users/tischer/Desktop/zarr-test",
                OMEZarrWriter.ImageType.Intensities,
                true );
        System.out.println("Saving time [ms]: " + ( System.currentTimeMillis() - start ));
    }
}
