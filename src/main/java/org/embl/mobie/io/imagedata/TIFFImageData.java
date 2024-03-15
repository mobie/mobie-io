package org.embl.mobie.io.imagedata;

import bdv.SpimSource;
import bdv.ViewerImgLoader;
import bdv.VolatileSpimSource;
import bdv.cache.SharedQueue;
import bdv.viewer.Source;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.ImagePlus;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.embl.mobie.io.util.IOHelper;
import org.embl.mobie.io.util.InputStreamXmlIoSpimData;
import org.embl.mobie.io.util.SharedQueueHelper;

import java.io.InputStream;

public class TIFFImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    public TIFFImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }

    @Override
    protected void open()
    {
        try
        {
            ImagePlus imagePlus = IOHelper.openTiffFromFile( uri );
            spimData = ImagePlusToSpimData.getSpimData( imagePlus );
            final BasicImgLoader imgLoader = spimData.getSequenceDescription().getImgLoader();
            if ( imgLoader instanceof ViewerImgLoader )
                ( ( ViewerImgLoader ) imgLoader ).setCreatedSharedQueue( sharedQueue );
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
