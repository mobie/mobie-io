package org.embl.mobie.io.imagedata;

import bdv.SpimSource;
import bdv.VolatileSpimSource;
import bdv.cache.SharedQueue;
import bdv.viewer.Source;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.Angle;
import mpicbg.spim.data.sequence.Channel;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;

public class SpimDataImageData< T extends NumericType< T > & NativeType< T > > implements ImageData< T >
{
    protected AbstractSpimData< ? > spimData;

    protected SharedQueue sharedQueue;

    protected boolean isOpen;

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
        // Should be overwritten by child classes

        if ( ! isOpen ) open();

        return null;
    }

    protected void open()
    {
        // Should be overwritten by child classes
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
