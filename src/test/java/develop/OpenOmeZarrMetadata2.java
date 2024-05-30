package develop;

import com.amazonaws.auth.AnonymousAWSCredentials;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.universe.N5DatasetDiscoverer;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.Executors;

import static org.janelia.saalfeldlab.n5.universe.N5DatasetDiscoverer.DEFAULT_GROUP_PARSERS;
import static org.janelia.saalfeldlab.n5.universe.N5DatasetDiscoverer.DEFAULT_PARSERS;

public class OpenOmeZarrMetadata2
{
    public static void main( String[] args ) throws URISyntaxException
    {
        String uri = "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr";
        N5URI n5URI = new N5URI( uri );
        String containerPath = n5URI.getContainerPath();
        N5Factory n5Factory = new N5Factory();
        n5Factory.s3UseCredentials( new AnonymousAWSCredentials() );
        N5Reader n5 = n5Factory.openReader( containerPath );

        N5DatasetDiscoverer n5DatasetDiscoverer = new N5DatasetDiscoverer(
                n5,
                Executors.newCachedThreadPool(),
                Arrays.asList( DEFAULT_PARSERS ),
                Arrays.asList( DEFAULT_GROUP_PARSERS )
        );
        // works:
        N5Metadata metadata = n5DatasetDiscoverer.parse( "s3" ).getMetadata();
        System.out.println( metadata );

        // returns null:
        metadata = n5DatasetDiscoverer.parse( "/" ).getMetadata();
        System.out.println( metadata );
        metadata = n5DatasetDiscoverer.parse( "labels/cells" ).getMetadata();
        System.out.println( metadata );
    }
}
