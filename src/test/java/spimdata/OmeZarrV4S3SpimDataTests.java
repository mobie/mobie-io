package spimdata;

import ij.IJ;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrOpener;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OmeZarrV4S3SpimDataTests < N extends NumericType< N > & RealType< N > >
{
    public static final String ZYX_FILE_KEY = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/zyx.ome.zarr";
    public static final String CZYX_FILE_KEY = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/czyx.ome.zarr";

    @Test
    public void SpimDataV4UnitTest() throws IOException {
        SpimData spimData = OMEZarrS3Opener.readURL(ZYX_FILE_KEY);

        final String unit = spimData.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize().unit();
        final double[] dimensions = new double[ 3 ];
        spimData.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize().dimensions( dimensions );

        assertEquals("nanometer", unit);
        assertArrayEquals( dimensions, new double[]{64.0, 64.0, 64.0});
    }

    @Test
    public void SpimDataV4MultiChannelTest() throws IOException {
        //SpimData spimData = OMEZarrS3Opener.readURL(CZYX_FILE_KEY);
        SpimData spimData = OMEZarrOpener.openFile( "/Users/tischer/Desktop/tischi-debug/data/Round1/images/ome-zarr/plate_01.ome.zarr/B/02/0" );

        final int numSetups = spimData.getSequenceDescription().getViewSetupsOrdered().size();
        System.out.println(CZYX_FILE_KEY);
        for ( int setupId = 0; setupId < numSetups; setupId++ )
        {
            printInfo( spimData, setupId );
        }

        //BdvFunctions.show( spimData );
    }


    @NotNull
    private void printInfo( SpimData spimData, int setupId )
    {
        final MultiResolutionSetupImgLoader< N > setupImgLoader = ( MultiResolutionSetupImgLoader ) spimData.getSequenceDescription().getImgLoader().getSetupImgLoader( setupId );
        final int numMipmapLevels = setupImgLoader.numMipmapLevels();
        final int level = numMipmapLevels - 1;
        final RandomAccessibleInterval< N > image = setupImgLoader.getImage( 0, level );
        final Cursor< N > cursor = Views.iterable( image ).cursor();
        final MinMax minMax = new MinMax();
        while ( cursor.hasNext() )
        {
            final N next = cursor.next();
            if ( next.getRealDouble() > minMax.max )
                minMax.max = next.getRealDouble();;
            if ( next.getRealDouble() < minMax.min )
                minMax.min = next.getRealDouble();;
        }
        System.out.println("SetupId: " + setupId);
        System.out.println("Level: " + level);
        System.out.println("Min: " + minMax.min);
        System.out.println("Max: " + minMax.max);
        System.out.println("Dimensions: " + Arrays.toString( image.dimensionsAsLongArray()) );
    }

    class MinMax {
        double min = Double.MAX_VALUE;
        double max  = - Double.MAX_VALUE;
    }
}
