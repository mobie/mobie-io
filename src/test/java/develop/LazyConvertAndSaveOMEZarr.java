package develop;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import org.embl.mobie.io.OMEZarrWriter;

public class LazyConvertAndSaveOMEZarr
{
    public static void main( String[] args )
    {
        // Open
        ImagePlus floatImp = IJ.openVirtual("/Users/tischer/Desktop/blobs-float.tif");

        // Lazy convert from float to short
        float min = 0.0F;
        float max = 255.0F;
        float scale = 65535 / ( max - min);
        RandomAccessibleInterval< FloatType > floatTypes = ImageJFunctions.wrapFloat( floatImp );
        RandomAccessibleInterval< UnsignedShortType > shortTypes =
                Converters.convert( floatTypes, ( floatType, shortType )
                    -> shortType.setReal( scale * ( floatType.get() - min ) ), new UnsignedShortType() );
        ImagePlus shortImp = ImageJFunctions.wrap( shortTypes, "converted image" );

        // Save
        OMEZarrWriter.write( shortImp, "/Users/tischer/Desktop/test.zarr", OMEZarrWriter.ImageType.Intensities, true );
    }
}
