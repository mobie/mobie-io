package develop;

import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource4D;
import org.janelia.saalfeldlab.n5.universe.metadata.FinalVoxelDimensions;

import java.lang.reflect.Field;

public class DeleteMe
{
    public static void main( String[] args ) throws NoSuchFieldException, IllegalAccessException
    {
        //RandomAccessibleIntervalSource4D< ? > source4D = new RandomAccessibleIntervalSource4D<>( null,null, null );
        Field field = AbstractSource.class.getDeclaredField("voxelDimensions");
        field.setAccessible(true);
        //field.set( source4D, new FinalVoxelDimensions( "pixel", 1.0, 1.0, 1.0 ) );
    }
}
