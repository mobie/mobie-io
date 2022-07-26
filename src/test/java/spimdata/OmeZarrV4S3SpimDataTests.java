package spimdata;

import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrOpener;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
        SpimData spimData = OMEZarrS3Opener.readURL(CZYX_FILE_KEY);

        final int numSetups = spimData.getSequenceDescription().getViewSetupsOrdered().size();
        for ( int setupId = 0; setupId < numSetups; setupId++ )
        {
            final MinMax minMax = getMinMax( spimData, setupId );
            System.out.println( "setup="+setupId);
            System.out.println( "min="+minMax.min);
            System.out.println( "max="+minMax.max);
        }

        //BdvFunctions.show( spimData );
    }

    @Test
    public void SpimDataV4MultiChannelTest2() throws IOException {
        //SpimData spimData = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/spatial-transcriptomics-example/pos42/images/ome-zarr/MMStack_Pos42.ome.zarr");
        final SpimData spimData = OMEZarrOpener.openFile( "/Volumes/kreshuk/pape/Work/playground/mobie-projects/spatial-trans/test.ome.zarr" );

        final int numSetups = spimData.getSequenceDescription().getViewSetupsOrdered().size();
        for ( int setupId = 0; setupId < numSetups; setupId++ )
        {
            final MinMax minMax = getMinMax( spimData, setupId );
            System.out.println( "setup="+setupId);
            System.out.println( "min="+minMax.min);
            System.out.println( "max="+minMax.max);
        }

        //BdvFunctions.show( spimData );
    }

    @NotNull
    private MinMax getMinMax( SpimData spimData, int setupId )
    {
        final MultiResolutionSetupImgLoader< N > setupImgLoader = ( MultiResolutionSetupImgLoader ) spimData.getSequenceDescription().getImgLoader().getSetupImgLoader( setupId );
        final int numMipmapLevels = setupImgLoader.numMipmapLevels();
        final RandomAccessibleInterval< N > image = setupImgLoader.getImage( 0, numMipmapLevels - 1 );
        final N type = Util.getTypeFromInterval( image );
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
        return minMax;
    }

    class MinMax {
        double min = Double.MAX_VALUE;
        double max  = - Double.MAX_VALUE;
    }
}
