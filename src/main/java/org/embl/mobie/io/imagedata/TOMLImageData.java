package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.ImagePlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.toml.TOMLOpener;

public class TOMLImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
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
            super.open();
        }
        catch ( Exception e )
        {
            System.err.println( "Error opening " + uri );
            throw new RuntimeException( e );
        }
    }
}
