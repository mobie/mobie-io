package develop;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.patcher.LegacyInjector;
import org.embl.mobie.io.OMEZarrWriter;

import static org.janelia.saalfeldlab.n5.ij.N5ScalePyramidExporter.GZIP_COMPRESSION;

public class OMEZarrSavingSpeed
{
    static
    {
        LegacyInjector.preinit();
    }

    public static void main( String[] args )
    {
        int size = 100; // => 2 s
        ImagePlus imp = IJ.createImage( "image", size, size, size, 8 );

        long start = System.currentTimeMillis();
        OMEZarrWriter.write( imp,
                "/Users/tischer/Desktop/zarr-test",
                OMEZarrWriter.ImageType.Intensities,
                true,
                GZIP_COMPRESSION );
        System.out.println("Saving time [ms]: " + ( System.currentTimeMillis() - start ));
    }
}
