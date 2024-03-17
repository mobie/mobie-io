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

public class SpimDataImageData< T extends NumericType< T > & NativeType< T > > implements ImageData< T >
{
    protected String uri;

    protected AbstractSpimData< ? > spimData;

    protected SharedQueue sharedQueue;

    protected boolean isOpen;


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
        if ( ! isOpen ) open();

        BasicViewSetup basicViewSetup = spimData.getSequenceDescription().getViewSetupsOrdered().get( datasetIndex );
        final String setupName = createSetupName( basicViewSetup );
        Pair< Source< T >, Source< ? extends Volatile< T > > > sourcePair =
                new ValuePair<>(
                        new SpimSource<>( spimData, datasetIndex, setupName ),
                        new VolatileSpimSource<>( spimData, datasetIndex, setupName ));

        return sourcePair;
    }

    @Override
    public int getNumDatasets()
    {
        if ( ! isOpen ) open();

        return spimData.getSequenceDescription().getViewSetupsOrdered().size();
    }

    @Override
    public CanonicalDatasetMetadata getMetadata( int datasetIndex )
    {
        if ( ! isOpen ) open();

        try
        {
            // Using bigdataviewer-spimdata-extras
            final Displaysettings displaysettings = spimData.getSequenceDescription().getViewSetupsOrdered().get( datasetIndex ).getAttribute( Displaysettings.class );

            int[] color = displaysettings.color;
            RGBAColorMetadata colorMetadata = new RGBAColorMetadata( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );

            return new CanonicalDatasetMetadata(
                    uri,
                    null,
                    displaysettings.min,
                    displaysettings.max,
                    colorMetadata
            );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    protected void open()
    {
        setSharedQueue( sharedQueue );
        isOpen = true;
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
}
