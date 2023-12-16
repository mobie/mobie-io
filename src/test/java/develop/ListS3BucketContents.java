package develop;

import org.embl.mobie.io.util.S3Utils;

import java.util.ArrayList;

public class ListS3BucketContents
{
    public static void main( String[] args )
    {
        ArrayList< String > s3FilePaths = S3Utils.getS3FilePaths( "https://s3.embl.de/i2k-2020/incu-test-data/2207/19" );
    }
}
