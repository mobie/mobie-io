package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.ImagePlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.toml.TOMLOpener;
import org.embl.mobie.io.util.SharedQueueHelper;

public class TOMLImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    private final String uri;

    public TOMLImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }

    @Override
    protected void open()
    {
        try
        {
            ImagePlus imagePlus = new TOMLOpener( uri ).openImagePlus();;
            spimData = ImagePlusToSpimData.getSpimData( imagePlus );
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
