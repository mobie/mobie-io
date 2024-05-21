package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.BdvOptions;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import com.amazonaws.auth.BasicAWSCredentials;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.embl.mobie.io.util.IOHelper;
import org.janelia.saalfeldlab.n5.*;
import org.janelia.saalfeldlab.n5.bdv.N5Viewer;
import org.janelia.saalfeldlab.n5.ui.DataSelection;
import org.janelia.saalfeldlab.n5.universe.N5DatasetDiscoverer;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.universe.N5MetadataUtils;
import org.janelia.saalfeldlab.n5.universe.N5TreeNode;
import org.janelia.saalfeldlab.n5.universe.metadata.IntColorMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.ome.ngff.v04.OmeNgffMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class N5ImageData< T extends NumericType< T > & NativeType< T > > extends AbstractImageData< T >
{
    private final String uri;
    private final SharedQueue sharedQueue;
    private final String[] s3AccessAndSecretKey;
    private boolean isOpen;
    private List< SourceAndConverter< T > > sourcesAndConverters;
    private int numTimepoints;
    private BdvOptions bdvOptions;
    private List< ConverterSetup > converterSetups;

    public N5ImageData( String uri )
    {
        this.uri = uri;
        this.sharedQueue = new SharedQueue( 1 );
        this.s3AccessAndSecretKey = null;
    }

    public N5ImageData( String uri, String[] s3AccessAndSecretKey )
    {
        this.uri = uri;
        this.sharedQueue = new SharedQueue( 1 );
        this.s3AccessAndSecretKey = s3AccessAndSecretKey;
    }

    public N5ImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
        this.s3AccessAndSecretKey = null;
    }

    public N5ImageData( String uri, SharedQueue sharedQueue, String[] s3AccessAndSecretKey )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
        this.s3AccessAndSecretKey = s3AccessAndSecretKey;
    }

    @Override
    public Pair< Source< T >, Source< ? extends Volatile< T > > > getSourcePair( int datasetIndex )
    {
        if ( !isOpen ) open();

        SourceAndConverter< T > sourceAndConverter = sourcesAndConverters.get( datasetIndex );

        Source< T > source = sourceAndConverter.getSpimSource();
        Source< ? extends Volatile< T > > vSource = sourceAndConverter.asVolatile().getSpimSource();

        Pair< Source< T >, Source< ? extends Volatile< T > > > sourcePair =
                new ValuePair<>(
                        source,
                        vSource );

        return sourcePair;
    }

    @Override
    public int getNumDatasets()
    {
        if ( !isOpen ) open();

        return sourcesAndConverters.size();
    }

    @Override
    public CanonicalDatasetMetadata getMetadata( int datasetIndex )
    {
        if ( !isOpen ) open();

        ConverterSetup converterSetup = converterSetups.get( datasetIndex );

        IntColorMetadata colorMetadata = new IntColorMetadata( converterSetup.getColor().get() );

        return new CanonicalDatasetMetadata(
                uri,
                null,
                converterSetup.getDisplayRangeMin(),
                converterSetup.getDisplayRangeMax(),
                colorMetadata
        );
    }

    public List< SourceAndConverter< T > > getSourcesAndConverters()
    {
        if ( !isOpen ) open();

        return sourcesAndConverters;
    }

    public int getNumTimepoints()
    {
        return numTimepoints;
    }

    public BdvOptions getBdvOptions()
    {
        return bdvOptions;
    }

    private synchronized void open()
    {
        if ( isOpen ) return;

        try
        {
            N5URI n5URI = new N5URI( uri );
            String containerPath = n5URI.getContainerPath();

            N5Factory n5Factory = new N5Factory();
            if( s3AccessAndSecretKey != null )
            {
                BasicAWSCredentials credentials = new BasicAWSCredentials( s3AccessAndSecretKey[ 0 ], s3AccessAndSecretKey[ 1 ] );
                n5Factory = n5Factory.s3UseCredentials( credentials );
            }

            // FIXME: This is really slow...and not always needed..
            N5Reader n5 = n5Factory.openReader( containerPath );
            String rootGroup = n5URI.getGroupPath() != null ? n5URI.getGroupPath() : "/";
            N5Metadata rootMetadata = N5MetadataUtils.parseMetadata( n5, rootGroup );
            List< String > groups = new ArrayList<>();
            groups.add( rootGroup );
            List< N5Metadata > metadata = groups.stream()
                    .map( group -> N5MetadataUtils.parseMetadata( n5, group ) )
                    .collect( Collectors.toList() );


            final N5TreeNode root = N5DatasetDiscoverer.discover( n5 );
            groups = N5TreeNode.flattenN5Tree( root )
                .filter( n5TreeNode ->
                {
                    final N5Metadata meta = n5TreeNode.getMetadata();
                    return meta instanceof OmeNgffMetadata;
                } )
                .map( N5TreeNode::getPath )
                // FIXME Ask John why the "/" needs to be removed
                .map( path -> path.startsWith("/") ? path.substring( 1 ) : path )
                .map( path -> path.isEmpty() ? "/" : path )
                .collect( Collectors.toList() );

            //String[] datasets = n5.deepList( uri );
            //String group = n5URI.getGroupPath() != null ? n5URI.getGroupPath() : "/";
            //String[] strings = n5.deepList( group );
            // = Collections.singletonList( N5MetadataUtils.parseMetadata( n5, group ) );

//            if ( groups.isEmpty() )
//            {
//                String rootGroup = n5URI.getGroupPath() != null ? n5URI.getGroupPath() : "/";
//                groups.add( rootGroup );
//            }

            metadata = groups.stream()
                    .map( group -> N5MetadataUtils.parseMetadata( n5, group ) )
                    .collect( Collectors.toList() );

            converterSetups = new ArrayList<>();
            sourcesAndConverters = new ArrayList<>();
            bdvOptions = BdvOptions.options().frameTitle( "" );

            for ( N5Metadata oneMetadata : metadata )
            {
                final DataSelection selection =
                        new DataSelection( n5, Collections.singletonList( oneMetadata ) );

                int numDatasets = sourcesAndConverters.size();

                numTimepoints = Math.max( numTimepoints, N5Viewer.buildN5Sources(
                        n5,
                        selection,
                        sharedQueue,
                        converterSetups,
                        sourcesAndConverters,
                        bdvOptions ) );

                int numChannels = sourcesAndConverters.size() - numDatasets;
                String path = oneMetadata.getPath();
                String name = path.replaceAll( "[/\\\\]", "_" );
                if ( numChannels > 1 )
                {
                    for ( int channelIndex = 0; channelIndex < numChannels; channelIndex++ )
                    {
                        String channelName = IOHelper.addChannelPostfix( name, channelIndex );
                        channelName = channelName.startsWith( "_" ) ? channelName.substring( 1 ) : channelName;
                        datasetNames.add( channelName );
                    }
                }
                else
                {
                    name = name.startsWith( "_" ) ? name.substring( 1 ) : name;
                    datasetNames.add( name  );
                }
            }

            if ( sourcesAndConverters.isEmpty() )
                throw new IOException( "N5ImageData: No datasets found." );


        }
        catch ( Exception e )
        {
            System.err.println( "N5ImageData: Error opening " + uri );
            e.printStackTrace();
            throw new RuntimeException( e );
        }

        isOpen = true;
    }

}
