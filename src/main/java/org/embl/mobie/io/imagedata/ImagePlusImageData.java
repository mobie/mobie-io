package org.embl.mobie.io.imagedata;

import bdv.SpimSource;
import bdv.VolatileSpimSource;
import bdv.cache.SharedQueue;
import bdv.viewer.Source;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.ImagePlus;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.embl.mobie.io.metadata.ImageMetadata;
import org.embl.mobie.io.util.SharedQueueHelper;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalSpatialDatasetMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.SpatialMetadataCanonical;

public class ImagePlusImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    private final ImagePlus imagePlus;

    public ImagePlusImageData( ImagePlus imagePlus, SharedQueue sharedQueue )
    {
        this.imagePlus = imagePlus;
        this.sharedQueue = sharedQueue;
        //new DatasetAttributes(  )
        //new CanonicalSpatialDatasetMetadata(  )
    }

    @Override
    protected void open()
    {
        try
        {
            spimData = ImagePlusToSpimData.getSpimData( imagePlus );
            SharedQueueHelper.setSharedQueue( sharedQueue, spimData );

            isOpen = true;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
