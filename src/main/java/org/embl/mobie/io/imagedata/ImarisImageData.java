package org.embl.mobie.io.imagedata;

import bdv.SpimSource;
import bdv.VolatileSpimSource;
import bdv.cache.SharedQueue;
import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import bdv.viewer.Source;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.ImagePlus;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.embl.mobie.io.util.IOHelper;
import org.embl.mobie.io.util.SharedQueueHelper;

public class ImarisImageData< T extends NumericType< T > & NativeType< T > > implements ImageData< T >
{
    private final String uri;
    private final SharedQueue sharedQueue;

    private boolean isOpen;
    private SpimDataMinimal spimData;

    public ImarisImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }

    @Override
    public Pair< Source< T >, Source< ? extends Volatile< T > > > getSourcePair( int datasetIndex, String name )
    {
        if ( !isOpen ) open();

        Pair< Source< T >, Source< ? extends Volatile< T > > > sourcePair =
                new ValuePair<>(
                        new SpimSource<>( spimData, datasetIndex, name ),
                        new VolatileSpimSource<>( spimData, datasetIndex, name ));

        return sourcePair;
    }

    private void open()
    {
        try
        {
            spimData = Imaris.openIms( uri );
            SharedQueueHelper.setSharedQueue( sharedQueue, spimData );

            isOpen = true;
        }
        catch ( Exception e )
        {
            System.err.println( "Error opening " + uri );
            throw new RuntimeException( e );
        }
    }
}
