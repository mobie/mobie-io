package develop;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class DevelopParseOmeZarrMetadata
{
    public static void main( String[] args ) throws URISyntaxException, IOException, ExecutionException, InterruptedException
    {
        String uri = "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr";
        //String uri = "/Users/tischer/Downloads/20200812-CardiomyocyteDifferentiation14-Cycle1_mip.zarr/B/03/0";
        long start;

        start = System.currentTimeMillis();
        N5Factory probeFactory = new N5Factory();
        probeFactory.s3Configuration( builder -> builder.credentialsProvider( AnonymousCredentialsProvider.create() ) );
        System.out.println( "N5Factory.s3Configuration(...): [ms] " + ( System.currentTimeMillis() - start ) );

        for ( int i = 0; i < 2; i++ )
        {
            start = System.currentTimeMillis();
            N5URI n5URI = new N5URI( uri );
            String containerPath = n5URI.getContainerPath();
            N5Factory n5Factory = new N5Factory();
            n5Factory.s3Configuration( builder -> builder.credentialsProvider( AnonymousCredentialsProvider.create() ) );
            N5Reader n5 = n5Factory.openReader( containerPath );
            System.out.println( "Opened reader of " + uri + " in [ms]: " + ( System.currentTimeMillis() - start ) );
        }

//        String group = n5URI.getGroupPath() != null ? n5URI.getGroupPath() : "/";
//        String[] strings = n5.deepList( group, Executors.newCachedThreadPool() );
//
//        // look labels
//        String labelsUri = IOHelper.combinePath( uri, "labels", ".zattrs" );
//        String labelsJSON = IOHelper.read( labelsUri );
//        System.out.println( labelsJSON );
//        Gson gson = new Gson();
//        Labels labels = gson.fromJson( labelsJSON, new TypeToken< Labels >() {}.getType() );
//
//        for ( String label : labels.labels )
//        {
//            start = System.currentTimeMillis();
//            String labelGroup = "labels/" + label;
//            System.out.println( "reading labels metadata from " + labelGroup + "...");
//            N5Metadata labelMetadata = N5MetadataUtils.parseMetadata( n5, labelGroup, false );
//            System.out.println( labelMetadata );
//            System.out.println( "Read label metadata from " + labelGroup + " in [ms]: " + ( System.currentTimeMillis() - start ) );
//
//            start = System.currentTimeMillis();
//            System.out.println( "reading labels metadata from " + labelGroup + "...");
//            N5DatasetDiscoverer n5DatasetDiscoverer = new N5DatasetDiscoverer(
//                    n5,
//                    Executors.newCachedThreadPool(),
//                    Arrays.asList( DEFAULT_PARSERS ),
//                    Arrays.asList( DEFAULT_GROUP_PARSERS )
//            );
//            N5Metadata metadata = n5DatasetDiscoverer.parse( "labels/cells" ).getMetadata();
//            N5Metadata metadata2 = n5DatasetDiscoverer.parse( "s3" ).getMetadata();
//            System.out.println( metadata );
//            System.out.println( "Read label metadata directly from " + labelGroup + " in [ms]: " + ( System.currentTimeMillis() - start ) );
//        }

    }
}
