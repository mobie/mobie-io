package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.OpenersToSpimData;
import ch.epfl.biop.bdv.img.bioformats.BioFormatsHelper;
import ch.epfl.biop.bdv.img.opener.OpenerSettings;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BioFormatsImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    public BioFormatsImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }

    @Override
    protected void open()
    {
        try
        {
            final File file = new File( uri );
            List< OpenerSettings > openerSettings = new ArrayList<>();
            int numSeries = BioFormatsHelper.getNSeries(file);
            for (int i = 0; i < numSeries; i++) {
                openerSettings.add(
                        OpenerSettings.BioFormats()
                                .location(file)
                                .setSerie(i) );
            }

            spimData = OpenersToSpimData.getSpimData( openerSettings );
            super.open();
        }
        catch ( Exception e )
        {
            System.err.println( "Error opening " + uri );
            throw new RuntimeException( e );
        }
    }
}
