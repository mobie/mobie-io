package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.OpenersToSpimData;
import ch.epfl.biop.bdv.img.bioformats.BioFormatsHelper;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ch.epfl.biop.bdv.img.opener.OpenerSettings;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BioFormatsImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    public BioFormatsImageData( SpimDataOpener spimDataOpener  )
    {
        super( spimDataOpener );
    }

    public BioFormatsImageData( String uri, SharedQueue sharedQueue )
    {
        super( new SpimDataOpener()
        {
            @Override
            public AbstractSpimData< ? > open( String uri )
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

                return OpenersToSpimData.getSpimData( openerSettings );
            }
        } );
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }
}
