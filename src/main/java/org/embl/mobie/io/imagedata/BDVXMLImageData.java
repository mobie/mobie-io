package org.embl.mobie.io.imagedata;

import bdv.SpimSource;
import bdv.VolatileSpimSource;
import bdv.cache.SharedQueue;
import bdv.viewer.Source;
import mpicbg.spim.data.SpimData;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.embl.mobie.io.util.InputStreamXmlIoSpimData;
import org.embl.mobie.io.util.IOHelper;
import org.embl.mobie.io.util.SharedQueueHelper;

import java.io.InputStream;

public class BDVXMLImageData< T extends NumericType< T > & NativeType< T > > implements ImageData< T >
{
    private final String uri;
    private final SharedQueue sharedQueue;

    private boolean isOpen;
    private SpimData spimData;

    public BDVXMLImageData( String uri, SharedQueue sharedQueue )
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
            InputStream stream = IOHelper.getInputStream( uri );
            spimData = new InputStreamXmlIoSpimData().open( stream, uri );
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
