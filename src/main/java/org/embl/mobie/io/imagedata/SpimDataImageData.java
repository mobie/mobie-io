package org.embl.mobie.io.imagedata;

import bdv.SpimSource;
import bdv.ViewerImgLoader;
import bdv.VolatileSpimSource;
import bdv.cache.SharedQueue;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.viewer.Source;
import ch.epfl.biop.bdv.img.CacheControlOverride;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.Angle;
import mpicbg.spim.data.sequence.Channel;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.janelia.saalfeldlab.n5.universe.metadata.RGBAColorMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;
import spimdata.util.Displaysettings;

public class SpimDataImageData< T extends NumericType< T > & NativeType< T > > extends AbstractImageData< T >
{
    protected String uri;

    protected AbstractSpimData< ? > spimData;

    protected SpimDataOpener spimDataOpener;

    protected SharedQueue sharedQueue;

    protected boolean isOpen;

    public SpimDataImageData( SpimDataOpener spimDataOpener )
    {
        this.spimDataOpener = spimDataOpener;
    }

    protected void setSharedQueue( SharedQueue sharedQueue )
    {
        BasicImgLoader imgLoader = spimData.getSequenceDescription().getImgLoader();

        if ( imgLoader instanceof CacheControlOverride )
        {
            CacheControlOverride cco = ( CacheControlOverride ) imgLoader;
            final VolatileGlobalCellCache volatileGlobalCellCache = new VolatileGlobalCellCache( sharedQueue );
            cco.setCacheControl( volatileGlobalCellCache );
        }
        else if ( imgLoader instanceof ViewerImgLoader )
        {
            ( ( ViewerImgLoader ) imgLoader ).setCreatedSharedQueue( sharedQueue );
        }
        else
        {
            // cannot set the sharedQueue
        }
    }

    @Override
    public Pair< Source< T >, Source< ? extends Volatile< T > > > getSourcePair( int datasetIndex )
    {
        if ( ! isOpen ) open( spimDataOpener, uri );

        // TODO: Why not return the same sourcePair if it is accessed a second time?
        String setupName = getName( datasetIndex );
        Pair< Source< T >, Source< ? extends Volatile< T > > > sourcePair =
                new ValuePair<>(
                        new SpimSource<>( spimData, datasetIndex, setupName ),
                        new VolatileSpimSource<>( spimData, datasetIndex, setupName ));

        return sourcePair;
    }

    @Override
    public int getNumDatasets()
    {
        if ( ! isOpen ) open( spimDataOpener, uri );

        return spimData.getSequenceDescription().getViewSetupsOrdered().size();
    }

    @Override
    public CanonicalDatasetMetadata getMetadata( int datasetIndex )
    {
        if ( ! isOpen ) open( spimDataOpener, uri );

        return metadata.get( datasetIndex );
    }

    protected synchronized void open( SpimDataOpener opener, String uri )
    {
        if ( isOpen ) return;

        try
        {
            spimData = opener.open( uri );
            setSharedQueue( sharedQueue );
            populateMetadata();
            populateDataSetNames();
            isOpen = true;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    private void populateMetadata()
    {
        int numSetups = spimData.getSequenceDescription().getViewSetupsOrdered().size();

        for ( int setupIndex = 0; setupIndex < numSetups; setupIndex++ )
        {
            try
            {
                // Using bigdataviewer-spimdata-extras
                final Displaysettings displaysettings = spimData.getSequenceDescription().getViewSetupsOrdered().get( setupIndex ).getAttribute( Displaysettings.class );

                int[] color = displaysettings.color;
                RGBAColorMetadata colorMetadata = new RGBAColorMetadata( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );

                CanonicalDatasetMetadata canonicalDatasetMetadata = new CanonicalDatasetMetadata(
                        uri,
                        null,
                        displaysettings.min,
                        displaysettings.max,
                        colorMetadata
                );

                metadata.add( canonicalDatasetMetadata );
            }
            catch ( Exception e )
            {
                // this happens if the data has not been produced with bdv-img-loaders
                metadata.add( null );
            }
        }

    }

    private void populateDataSetNames()
    {
        int numSetups = spimData.getSequenceDescription().getViewSetupsOrdered().size();
        for ( int setupIndex = 0; setupIndex < numSetups; setupIndex++ )
        {
            BasicViewSetup basicViewSetup = spimData.getSequenceDescription().getViewSetupsOrdered().get( setupIndex );
            final String setupName = createSetupName( basicViewSetup );
            datasetNames.add( setupName );
        }
    }


    private static String createSetupName( final BasicViewSetup setup )
    {
        if ( setup.hasName() )
            return setup.getName();

        String name = "";

        final Angle angle = setup.getAttribute( Angle.class );
        if ( angle != null )
            name += ( name.isEmpty() ? "" : " " ) + "a " + angle.getName();

        final Channel channel = setup.getAttribute( Channel.class );
        if ( channel != null )
            name += ( name.isEmpty() ? "" : " " ) + "c " + channel.getName();

        return name;
    }

    @Override
    public String getName( int datasetIndex )
    {
        if ( ! isOpen ) open( spimDataOpener, uri );

        return datasetNames.get( datasetIndex );
    }

}
