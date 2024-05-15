package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.ImagePlus;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.util.IOHelper;

public class BioFormatsS3ImageData< T extends NumericType< T > & NativeType< T > > extends BioFormatsImageData< T >
{
    public BioFormatsS3ImageData( String uri, SharedQueue sharedQueue )
    {
        super( new SpimDataOpener()
        {
            @Override
            public AbstractSpimData open( String uri ) throws Exception
            {
                ImagePlus imagePlus = IOHelper.openWithBioFormatsFromS3( uri, 0 );
                return ImagePlusToSpimData.getSpimData( imagePlus );
            }
        } );

        this.sharedQueue = sharedQueue;
        this.uri = uri;
    }
}
