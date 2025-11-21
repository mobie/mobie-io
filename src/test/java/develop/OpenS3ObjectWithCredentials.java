package develop;

import org.embl.mobie.io.util.IOHelper;

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
