package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.OpenersToSpimData;
import ch.epfl.biop.bdv.img.bioformats.BioFormatsHelper;
import ch.epfl.biop.bdv.img.opener.OpenerSettings;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.util.SharedQueueHelper;
import org.janelia.saalfeldlab.n5.universe.metadata.RGBAColorMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalSpatialDatasetMetadata;
import spimdata.util.Displaysettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BioFormatsImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    protected final String uri;

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

    @Override
    public CanonicalSpatialDatasetMetadata getMetadata( int datasetIndex )
    {
        if ( ! isOpen ) open();

        // Using bigdataviewer-spimdata-extras
        final Displaysettings displaysettings = spimData.getSequenceDescription().getViewSetupsOrdered().get( datasetIndex ).getAttribute( Displaysettings.class );

        int[] color = displaysettings.color;
        RGBAColorMetadata colorMetadata = new RGBAColorMetadata( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );

        new CanonicalDatasetMetadata(
                uri,
                null,
                displaysettings.min,
                displaysettings.max,
                colorMetadata
        );

        return null;
    }
}
