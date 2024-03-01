package org.embl.mobie.io.imagedata;

import bdv.SpimSource;
import bdv.VolatileSpimSource;
import bdv.cache.SharedQueue;
import bdv.viewer.Source;
import ch.epfl.biop.bdv.img.OpenersToSpimData;
import ch.epfl.biop.bdv.img.bioformats.BioFormatsHelper;
import ch.epfl.biop.bdv.img.opener.OpenerSettings;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.embl.mobie.io.metadata.ImageMetadata;
import org.embl.mobie.io.util.SharedQueueHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BioFormatsImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    private final String uri;

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
            SharedQueueHelper.setSharedQueue( sharedQueue, spimData );

            isOpen = true;
        }
        catch ( Exception e )
        {
            System.err.println( "Error opening " + uri );
            throw new RuntimeException( e );
        }
    }
}
