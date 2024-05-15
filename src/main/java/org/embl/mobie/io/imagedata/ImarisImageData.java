package org.embl.mobie.io.imagedata;

import bdv.ViewerImgLoader;
import bdv.cache.SharedQueue;
import bdv.img.imaris.Imaris;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

import java.io.IOException;

public class ImarisImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{

    public ImarisImageData( String uri, SharedQueue sharedQueue )
    {
        super( new SpimDataOpener()
        {
            @Override
            public AbstractSpimData open( String uri ) throws IOException
            {
                return Imaris.openIms( uri );
            }
        } );

        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }

}
