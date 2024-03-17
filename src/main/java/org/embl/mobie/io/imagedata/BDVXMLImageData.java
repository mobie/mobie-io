package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.util.IOHelper;
import org.embl.mobie.io.util.InputStreamXmlIoSpimData;

import java.io.InputStream;

public class BDVXMLImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    public BDVXMLImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }


    @Override
    protected void open()
    {
        try
        {
            InputStream stream = IOHelper.getInputStream( uri );
            spimData = new InputStreamXmlIoSpimData().open( stream, uri );
            super.open();
        }
        catch ( Exception e )
        {
            System.err.println( "Error opening " + uri );
            throw new RuntimeException( e );
        }
    }
}
