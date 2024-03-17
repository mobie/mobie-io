package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

import java.io.IOException;

public class ImageJImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{

    public ImageJImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }

    @Override
    protected void open()
    {
        try
        {
            ImagePlus imagePlus = IJ.openImage( uri );
            if ( imagePlus == null )
                throw new IOException();

            super.open();
        }
        catch ( Exception e )
        {
            System.err.println( "Error opening " + uri );
            throw new RuntimeException( e );
        }
    }
}
