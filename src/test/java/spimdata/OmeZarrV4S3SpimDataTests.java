package spimdata;

import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OmeZarrV4S3SpimDataTests < N extends NumericType< N > & RealType< N > >
{
    public static final String ZYX_FILE_KEY = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/zyx.ome.zarr";
    public static final String CZYX_FILE_KEY = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/czyx.ome.zarr";
    public static final String CYX_FILE_KEY = "https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/cyx.ome.zarr";

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
    public void SpimDataV4MultiChannelTestCZYX() throws IOException {
        System.out.println(CZYX_FILE_KEY);
        SpimData spimData = OMEZarrS3Opener.readURL(CZYX_FILE_KEY);
        //SpimData spimData = OMEZarrOpener.openFile( "/Users/tischer/Desktop/tischi-debug/data/Round1/images/ome-zarr/plate_01.ome.zarr/B/02/0" );


        // TODO: tricky: numTimepoints
        final int numSetups = spimData.getSequenceDescription().getViewSetupsOrdered().size();
        assertEquals(2, numSetups);

        for (int setupId = 0; setupId < numSetups; setupId++)
        {
            System.out.println("setup: " + setupId);
            final Info info = getImgInfo(spimData, setupId);
            info.print();
            assertArrayEquals(new long[]{128, 66, 122}, info.dimensions);
            assertEquals(3, info.levels);
            if (setupId == 0)
                assertEquals(5115.0, info.max);
            else if (setupId == 1)
                assertEquals(280.0, info.max);
        }
    }

    @Test
    public void SpimDataV4MultiChannelTestCYX() throws IOException {
        System.out.println( CYX_FILE_KEY );
        SpimData spimData = OMEZarrS3Opener.readURL( CYX_FILE_KEY );
        final int numSetups = spimData.getSequenceDescription().getViewSetupsOrdered().size();
        //assertEquals( 2, numSetups );

        for ( int setupId = 0; setupId < numSetups; setupId++ )
        {
            System.out.println("setup: " + setupId);
            final Info info = getImgInfo( spimData, setupId );
            info.print();
            //assertArrayEquals( new long[]{128, 66, 122}, info.dimensions );
            //assertEquals( 3, info.levels );
//            if ( setupId == 0 )
//                assertEquals( 5115.0, info.max );
//            else if ( setupId == 1 )
//                assertEquals( 280.0, info.max );
        }
    }

    private Info getImgInfo( SpimData spimData, int setupId )
    {
        // TODO we could add a method getNumTimepoints() to our ImageLoader?
        //   Then we could use this in the tests
        final MultiResolutionSetupImgLoader< N > setupImgLoader = ( MultiResolutionSetupImgLoader ) spimData.getSequenceDescription().getImgLoader().getSetupImgLoader( setupId );
        final int numMipMapLevels = setupImgLoader.numMipmapLevels();
        final int level = numMipMapLevels - 1;

        final RandomAccessibleInterval< N > image = setupImgLoader.getImage( 0, level );
        final Cursor< N > cursor = Views.iterable( image ).cursor();
        final Info info = new Info();
        while ( cursor.hasNext() )
        {
            final N next = cursor.next();
            if ( next.getRealDouble() > info.max )
                info.max = next.getRealDouble();;
            if ( next.getRealDouble() < info.min )
                info.min = next.getRealDouble();;
        }
        info.dimensions = image.dimensionsAsLongArray();
        info.level = level;
        info.levels = numMipMapLevels;

        return info;
    }

    class Info // for one setup
    {
        public int levels;
        public int level; // lowest resolution level
        double min = Double.MAX_VALUE; // at lowest resolution level
        double max = -Double.MAX_VALUE; // at lowest resolution level
        long[] dimensions; // at lowest resolution level

        public void print()
        {
            System.out.println("Levels: " + levels );
            System.out.println("Lowest level: " + level);
            System.out.println("Min: " + min);
            System.out.println("Max: " + max);
            System.out.println("Dimensions: " + Arrays.toString( dimensions ) );
        }
    }
}
