package develop;

import software.amazon.awssdk.services.s3.S3Client;

public class DebugSlowAmazonS3ClientBuilder
{
    public static void main( String[] args )
    {
        long start;
        S3Client s3Client;

        for ( int i = 0; i < 2; i++ )
        {
            start = System.currentTimeMillis();
            s3Client = S3Client.builder().build();
            System.out.println( "S3Client.builder().build(): [ms] " + ( System.currentTimeMillis() - start ) );
            s3Client.close();
        }
    }
}
