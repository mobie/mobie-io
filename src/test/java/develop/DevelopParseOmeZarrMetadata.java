package develop;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.embl.mobie.io.ngff.Labels;
import org.embl.mobie.io.util.IOHelper;
import org.janelia.saalfeldlab.n5.N5DatasetDiscoverer;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.universe.N5MetadataUtils;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;
import org.jruby.RubyProcess;
import sun.lwawt.macosx.CSystemTray;

import java.io.IOException;
import java.net.URISyntaxException;

public class DevelopParseOmeZarrMetadata
{
    public static void main( String[] args ) throws URISyntaxException, IOException
    {
        //String uri = "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr";
        String uri = "/Users/tischer/Downloads/20200812-CardiomyocyteDifferentiation14-Cycle1_mip.zarr/B/03/0";
        long start;

        start = System.currentTimeMillis();
        N5URI n5URI = new N5URI( uri );
        String containerPath = n5URI.getContainerPath();
        N5Factory n5Factory = new N5Factory();
        N5Reader n5 = n5Factory.openReader( containerPath );
        String group = n5URI.getGroupPath() != null ? n5URI.getGroupPath() : "/";
        System.out.println( "Opened reader of " + uri + " in [ms]: " + ( System.currentTimeMillis() - start ) );

        start = System.currentTimeMillis();
        N5Metadata metadata = N5MetadataUtils.parseMetadata( n5, group, false );
        // FIXME: https://github.com/saalfeldlab/n5-universe/issues/21
        System.out.println( "Read metadata from " + group + " in [ms]: " + ( System.currentTimeMillis() - start ) );

        System.out.println( "Metadata :" + metadata );

        // TODO: look for labels (using different methods)
        start = System.currentTimeMillis();
        String labelsUri = IOHelper.combinePath( uri, "labels", ".zattrs" );
        String labelsJSON = IOHelper.read( labelsUri );
        System.out.println( labelsJSON );
        Gson gson = new Gson();
        Labels labels = gson.fromJson( labelsJSON, new TypeToken< Labels >() {}.getType() );

        for ( String label : labels.labels )
        {
            start = System.currentTimeMillis();
            String labelGroup = "labels/" + label;
            System.out.println( "reading labels metadata from " + labelGroup + "...");
            N5Metadata labelMetadata = N5MetadataUtils.parseMetadata( n5, labelGroup, false );
            System.out.println( labelMetadata );
            System.out.println( "Read labels from " + labelGroup + " in [ms]: " + ( System.currentTimeMillis() - start ) );
        }

    }
}
