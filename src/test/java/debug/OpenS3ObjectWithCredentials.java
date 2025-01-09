package debug;

import bdv.cache.SharedQueue;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.embl.mobie.io.imagedata.ImageData;
import org.embl.mobie.io.util.IOHelper;

import java.util.Arrays;

public class OpenS3ObjectWithCredentials
{
    public static void main( String[] args )
    {
        for ( int i = 0; i < 5; i++ )
        {
            boolean exists = IOHelper.exists( "https://s3.gwdg.de/fruitfly-larva-em/project.json" );
            System.out.println( "exists: " + exists );
        }
    }
}
