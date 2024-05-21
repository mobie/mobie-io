package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

import java.io.IOException;

public class ImageJImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{

    public ImageJImageData( String uri, SharedQueue sharedQueue )
    {
        super( new SpimDataOpener()
        {
            @Override
            public AbstractSpimData< ? > open( String uri ) throws IOException
            {
                ImagePlus imagePlus = IJ.openImage( uri );
                if ( imagePlus == null )
                    throw new IOException();

                return ImagePlusToSpimData.getSpimData( imagePlus );
            }
        } );
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }
}
