package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.BdvOptions;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.embl.mobie.io.metadata.ImageMetadata;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.bdv.N5Viewer;
import org.janelia.saalfeldlab.n5.ui.DataSelection;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.universe.N5MetadataUtils;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class N5ImageData< T extends NumericType< T > & NativeType< T > > implements ImageData< T >
{
    private final String uri;
    private final SharedQueue sharedQueue;

    private boolean isOpen;
    private List< SourceAndConverter< T > > sourcesAndConverters;
    private int numTimepoints;
    private BdvOptions bdvOptions;

    public N5ImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }

    @Override
    public Pair< Source< T >, Source< ? extends Volatile< T > > > getSourcePair( int datasetIndex, String name )
    {
        if ( !isOpen ) open();

        SourceAndConverter< T > sourceAndConverter = sourcesAndConverters.get( datasetIndex );

        Source< T > source = sourceAndConverter.getSpimSource();
        Source< ? extends Volatile< T > > vSource = sourceAndConverter.asVolatile().getSpimSource();

        // FIXME: set the name of the sources!

        Pair< Source< T >, Source< ? extends Volatile< T > > > sourcePair =
                new ValuePair<>(
                        source,
                        vSource );

        return sourcePair;
    }

    public List< SourceAndConverter< T > > getSourcesAndConverters()
    {
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

    private void open()
    {
        try
        {
            N5URI n5URI = new N5URI( uri );
            String containerPath = n5URI.getContainerPath();
            String group = n5URI.getGroupPath() != null ? n5URI.getGroupPath() : "/";
            N5Reader n5 = new N5Factory().openReader( containerPath );
            List< N5Metadata > metadata = Collections.singletonList( N5MetadataUtils.parseMetadata( n5, group ) );

            final DataSelection selection = new DataSelection( n5, metadata );
            final List< ConverterSetup > converterSetups = new ArrayList<>();
            sourcesAndConverters = new ArrayList<>();
            bdvOptions = BdvOptions.options().frameTitle( "" );

            numTimepoints = N5Viewer.buildN5Sources(
                    n5,
                    selection,
                    sharedQueue,
                    converterSetups,
                    sourcesAndConverters,
                    bdvOptions );

            isOpen = true;
        }
        catch ( Exception e )
        {
            System.err.println( "Error opening " + uri);
            throw new RuntimeException( e );
        }
    }
}
