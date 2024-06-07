package develop;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class DebugSlowAmazonS3ClientBuilder
{
    public static void main( String[] args )
    {
        long start;
        AmazonS3ClientBuilder standard;

        for ( int i = 0; i < 2; i++ )
        {
            start = System.currentTimeMillis();
            standard = AmazonS3ClientBuilder.standard();
            System.out.println( "AmazonS3ClientBuilder.standard(): [ms] " + ( System.currentTimeMillis() - start ) );
        }
    }
}
