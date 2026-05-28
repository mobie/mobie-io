package org.embl.mobie.io.imagedata;

import bdv.BigDataViewer;
import bdv.cache.SharedQueue;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.BdvOptions;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import com.amazonaws.auth.BasicAWSCredentials;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.embl.mobie.io.ngff.Labels;
import org.embl.mobie.io.util.IOHelper;
import org.embl.mobie.io.zarrjava.ZarrJavaPyramidBackend;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.bdv.N5Viewer;
import org.janelia.saalfeldlab.n5.ui.DataSelection;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.universe.N5MetadataUtils;
import org.janelia.saalfeldlab.n5.universe.metadata.IntColorMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.ome.ngff.v04.OmeNgffMetadata;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZarrJavaImageData< T extends RealType< T > & NativeType< T >, V extends Volatile< T > & NativeType< V > & RealType< V > > extends AbstractImageData< T >
{
    private final String uri;
    private final SharedQueue sharedQueue;
    private final String[] s3AccessAndSecretKey;
    private boolean isOpen;
    private List< SourceAndConverter< T > > sourcesAndConverters;
    private int numTimePoints;
    private List< ConverterSetup > converterSetups;
    private final List< String > datasetPaths = new ArrayList<>();
    private final BdvOptions bdvOptions = BdvOptions.options();
    private List load;
    private List< SourceAndConverter< T > > sourceAndConverters;

    public ZarrJavaImageData( String uri )
    {
        this.uri = uri;
        this.sharedQueue = new SharedQueue( 1 );
        this.s3AccessAndSecretKey = null;
    }

    public ZarrJavaImageData( String uri, String[] s3AccessAndSecretKey )
    {
        this.uri = uri;
        this.sharedQueue = new SharedQueue( 1 );
        this.s3AccessAndSecretKey = s3AccessAndSecretKey;
    }

    public ZarrJavaImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
        this.s3AccessAndSecretKey = null;
    }

    public ZarrJavaImageData( String uri, SharedQueue sharedQueue, String[] s3AccessAndSecretKey )
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

        ZarrJavaPyramidBackend< T, V > pyramidBackend = null;
        try
        {
            pyramidBackend = new ZarrJavaPyramidBackend< T, V >( new URI( uri ) );
        } catch ( URISyntaxException e )
        {
            throw new RuntimeException( e );
        }

        sourceAndConverters = pyramidBackend.load();

        converterSetups = new ArrayList<>();
        for ( int setupIndex = 0; setupIndex < sourceAndConverters.size(); setupIndex++ )
        {
            converterSetups.add(
                BigDataViewer.createConverterSetup( sourceAndConverters.get( setupIndex ), setupIndex )
            );
        }

        isOpen = true;
    }
}
