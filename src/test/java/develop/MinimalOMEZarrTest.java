package develop;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.universe.N5MetadataUtils;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

public class MinimalOMEZarrTest
{
    public static void main( String[] args ) throws URISyntaxException
    {
        String uri = "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr";
        N5URI n5URI = new N5URI( uri );
        String containerPath = n5URI.getContainerPath();
        N5Factory n5Factory = new N5Factory();
        N5Reader n5 = n5Factory.openReader( containerPath );
        String group = n5URI.getGroupPath() != null ? n5URI.getGroupPath() : "/";
        N5Metadata metadata = N5MetadataUtils.parseMetadata( n5, group );
    }
}
