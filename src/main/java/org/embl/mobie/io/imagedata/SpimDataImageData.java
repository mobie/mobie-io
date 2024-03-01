package org.embl.mobie.io.imagedata;

import bdv.SpimSource;
import bdv.VolatileSpimSource;
import bdv.cache.SharedQueue;
import bdv.viewer.Source;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

public class SpimDataImageData< T extends NumericType< T > & NativeType< T > > implements ImageData< T >
{
    protected AbstractSpimData< ? > spimData;

    protected SharedQueue sharedQueue;

    protected boolean isOpen;

    @Override
    public Pair< Source< T >, Source< ? extends Volatile< T > > > getSourcePair( int datasetIndex, String name )
    {
        if ( ! isOpen ) open();

        Pair< Source< T >, Source< ? extends Volatile< T > > > sourcePair =
                new ValuePair<>(
                        new SpimSource<>( spimData, datasetIndex, name ),
                        new VolatileSpimSource<>( spimData, datasetIndex, name ));

        return sourcePair;
    }

    protected void open()
    {
        // Should be overwritten by child classes
    }
}
