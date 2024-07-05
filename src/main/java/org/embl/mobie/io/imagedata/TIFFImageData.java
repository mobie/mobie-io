package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.util.IOHelper;

public class TIFFImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    public TIFFImageData( String uri, SharedQueue sharedQueue )
    {
        super( new SpimDataOpener()
        {
            @Override
            public AbstractSpimData< ? > open( String uri )
            {
                ImagePlus imagePlus = IJ.openVirtual( uri );
                //ImagePlus imagePlus = IOHelper.openTiffFromFile( uri );
                return ImagePlusToSpimData.getSpimData( imagePlus );
            }
        } );

        this.uri = uri;
        this.sharedQueue = sharedQueue;

    }
}
