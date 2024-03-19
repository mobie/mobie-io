package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.ImagePlus;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.toml.TOMLOpener;

public class TOMLImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    public TOMLImageData( String uri, SharedQueue sharedQueue )
    {
        super( new SpimDataOpener()
        {
            @Override
            public AbstractSpimData open( String uri ) throws Exception
            {
                ImagePlus imagePlus = new TOMLOpener( uri ).openImagePlus();;
                return ImagePlusToSpimData.getSpimData( imagePlus );
            }
        } );
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }
}
