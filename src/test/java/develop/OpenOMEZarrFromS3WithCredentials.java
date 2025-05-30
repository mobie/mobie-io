package develop;

import bdv.cache.SharedQueue;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.embl.mobie.io.imagedata.ImageData;

import java.util.Arrays;

public class OpenOMEZarrFromS3WithCredentials
{
    public static void main( String[] args )
    {
        ImageDataFormat omeZarr = ImageDataFormat.OmeZarr;
        // If the below is commented it will look in ~/.aws/credentials
        omeZarr.setS3SecretAndAccessKey( new String[]{
                "a",
                "b"} );
        ImageData< ? > imageData = ImageDataOpener.open(
                "https://s3.gwdg.de/fruitfly-larva-em/igor_S2/images/ome-zarr/raw.ome.zarr",
                omeZarr,
                new SharedQueue( 4 ) );
        long[] dimensions = imageData.getSourcePair( 0 ).getA().getSource( 0, 0 ).dimensionsAsLongArray();
        System.out.println( Arrays.toString( dimensions ));
    }
}
