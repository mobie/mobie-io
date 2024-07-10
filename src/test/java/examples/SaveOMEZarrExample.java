package examples;

import bdv.cache.SharedQueue;
import bdv.util.BdvFunctions;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import org.embl.mobie.io.OMEZarrWriter;
import org.embl.mobie.io.imagedata.N5ImageData;

public class SaveOMEZarrExample
{
    public static void main( String[] args )
    {
        new ImageJ().ui().showUI(); // initialise services such that progress bar can be shown

        ImagePlus imp = IJ.createImage( "image", 500, 500, 500, 8 );

        long start = System.currentTimeMillis();
        OMEZarrWriter.write( imp,
                "/Users/tischer/Desktop/zarr-test",
                OMEZarrWriter.ImageType.Intensities,
                true );
        System.out.println("Saving time [ms]: " + ( System.currentTimeMillis() - start ));
    }
}
