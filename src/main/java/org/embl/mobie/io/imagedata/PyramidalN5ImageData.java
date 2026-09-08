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
import ome.zarr.fiji.PyramidalBdv;
import ome.zarr.fiji.read.ZarrReader;
import ome.zarr.imglib2.PyramidContents;
import ome.zarr.n5.N5PyramidBackend;
import org.embl.mobie.io.ContextProvider;
import org.embl.mobie.io.util.IOHelper;
import org.janelia.saalfeldlab.n5.universe.metadata.IntColorMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;
import org.scijava.Context;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PyramidalN5ImageData< T extends NumericType< T > & NativeType< T > > extends AbstractImageData< T >
{
    private final String uri;
    private final SharedQueue sharedQueue;
    private final String[] s3AccessAndSecretKey;
    private boolean isOpen;
    private List< ? extends SourceAndConverter< ? > > sourcesAndConverters;
    private int numTimePoints;
    private List< ConverterSetup > converterSetups;
    private final List< String > datasetPaths = new ArrayList<>();
    private final BdvOptions bdvOptions = BdvOptions.options();

    public PyramidalN5ImageData( String uri )
    {
        this.uri = uri;
        this.sharedQueue = new SharedQueue( 1 );
        this.s3AccessAndSecretKey = null;
    }

    public PyramidalN5ImageData( String uri, String[] s3AccessAndSecretKey )
    {
        this.uri = uri;
        this.sharedQueue = new SharedQueue( 1 );
        this.s3AccessAndSecretKey = s3AccessAndSecretKey;
    }

    public PyramidalN5ImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
        this.s3AccessAndSecretKey = null;
    }

    public PyramidalN5ImageData( String uri, SharedQueue sharedQueue, String[] s3AccessAndSecretKey )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
        this.s3AccessAndSecretKey = s3AccessAndSecretKey;
    }

    @Override
    public Pair< Source< T >, Source< ? extends Volatile< T > > > getSourcePair( int datasetIndex )
    {
        if ( !isOpen ) open();

        SourceAndConverter< ? > sourceAndConverter = sourcesAndConverters.get( datasetIndex );

        Source< ? > source = sourceAndConverter.getSpimSource();
        Source< ? extends Volatile< ? > > vSource = sourceAndConverter.asVolatile().getSpimSource();

        Pair< Source< T >, Source< ? extends Volatile< T > > > sourcePair =
                new ValuePair(
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

    public List< ? extends SourceAndConverter< ? > > getSourcesAndConverters()
    {
        if ( !isOpen ) open();

        return sourcesAndConverters;
    }

    public int getNumTimepoints()
    {
        if ( !isOpen ) open();

        return numTimePoints;
    }

    public String getPath( int datasetIndex )
    {
        return datasetPaths.get( datasetIndex );
    }

    public BdvOptions getBdvOptions()
    {
        if ( ! isOpen ) open();

        return bdvOptions;
    }

    private synchronized void open()
    {
        if ( isOpen ) return;

        N5PyramidBackend backendZarrJava = new N5PyramidBackend();
        ZarrReader reader = new ZarrReader( IOHelper.stringToUri( uri ), ContextProvider.getContext(), backendZarrJava );

        PyramidContents< ? > pyramidContents = reader.getContents();

        PyramidalBdv< ? > bdvFriendlyPyramid = new PyramidalBdv<>( ContextProvider.getContext(), pyramidContents );

        sourcesAndConverters = bdvFriendlyPyramid.asSources();

        // fetch metadata
        // bdvFriendlyPyramid.getPyramidContents().omero.channels.get( 0 ).

        isOpen = true;

    }
}
