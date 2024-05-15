package develop;

import bdv.cache.SharedQueue;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.viewer.SourceAndConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.bdv.N5Viewer;
import org.janelia.saalfeldlab.n5.ui.DataSelection;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.universe.N5MetadataUtils;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenOMEZarrWithN5Viewer
{
    public static <T extends NumericType<T> & NativeType<T> > void main( String[] args ) throws IOException, URISyntaxException
    {
        N5URI uri = new N5URI( "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr" );
        String containerPath = uri.getContainerPath();
        String group = uri.getGroupPath() != null ? uri.getGroupPath() : "/";
        N5Reader n5 = new N5Factory().openReader( containerPath );
        List< N5Metadata > metadata = Collections.singletonList( N5MetadataUtils.parseMetadata( n5, group ) );

        final DataSelection selection = new DataSelection(n5, metadata);
        final SharedQueue sharedQueue = new SharedQueue(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
        final List< ConverterSetup > converterSetups = new ArrayList<>();
        final List< SourceAndConverter<T> > sourcesAndConverters = new ArrayList<>();
        final BdvOptions options = BdvOptions.options().frameTitle("N5 Viewer");

        int numTimepoints = N5Viewer.buildN5Sources(
                n5,
                selection,
                sharedQueue,
                converterSetups,
                sourcesAndConverters,
                options );

        BdvFunctions.show(sourcesAndConverters, numTimepoints, options);
    }
}
