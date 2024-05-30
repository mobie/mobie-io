package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.OpenersToSpimData;
import ch.epfl.biop.bdv.img.bioformats.BioFormatsHelper;
import ch.epfl.biop.bdv.img.opener.OpenerSettings;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.util.IOHelper;
import org.embl.mobie.io.util.InputStreamXmlIoSpimData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BDVXMLImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    public BDVXMLImageData( String uri, SharedQueue sharedQueue )
    {
        super( new SpimDataOpener()
        {
            @Override
            public AbstractSpimData< ? > open( String uri ) throws Exception
            {
                InputStream stream = IOHelper.getInputStream( uri );
                return new InputStreamXmlIoSpimData().open( stream, uri );
            }
        } );
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }
}
