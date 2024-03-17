package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.ImagePlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.util.IOHelper;

public class BioFormatsS3ImageData< T extends NumericType< T > & NativeType< T > > extends BioFormatsImageData< T >
{
    public BioFormatsS3ImageData( String uri, SharedQueue sharedQueue )
    {
        super( uri, sharedQueue );
    }

    @Override
    protected void open()
    {
        try
        {
            ImagePlus imagePlus = IOHelper.openWithBioFormatsFromS3( uri, 0 );
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
