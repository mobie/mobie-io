package org.embl.mobie.io.imagedata;

import bdv.ViewerImgLoader;
import bdv.cache.SharedQueue;
import bdv.img.imaris.Imaris;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

public class ImarisImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{

    public ImarisImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }

    @Override
    protected void open()
    {
        try
        {
            spimData = Imaris.openIms( uri );
            super.open();
        }
        catch ( Exception e )
        {
            System.err.println( "Error opening " + uri );
            throw new RuntimeException( e );
        }
    }
}
