package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageStatistics;
import ij.process.LUT;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.util.SharedQueueHelper;
import org.janelia.saalfeldlab.n5.universe.metadata.RGBAColorMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;

import static ij.measure.Measurements.MIN_MAX;

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

    @Override
    public CanonicalDatasetMetadata getMetadata( int datasetIndex )
    {
        if ( ! isOpen ) open();

        imagePlus.setC( datasetIndex + 1 );
        LUT lut = imagePlus.getLuts()[ datasetIndex ];

        RGBAColorMetadata colorMetadata = new RGBAColorMetadata(
                lut.getRed( 255 ),
                lut.getGreen( 255 ),
                lut.getBlue( 255 ),
                lut.getAlpha( 255 ) );

        try
        {
            ImageStatistics statistics = ImageStatistics.getStatistics( imagePlus.getProcessor(), MIN_MAX, null );
            new ContrastEnhancer().stretchHistogram( imagePlus.getProcessor(), 0.35, statistics );
        }
        catch ( Exception e )
        {
            // https://forum.image.sc/t/b-c-for-a-whole-virtual-stack-cont/57811/12
            IJ.log( "[WARNING] Could not set auto-contrast for + " + imagePlus.getTitle() + " + due to: https://forum.image.sc/t/b-c-for-a-whole-virtual-stack-cont/57811/12" );
        }

        return new CanonicalDatasetMetadata(
                null,
                null,
                imagePlus.getProcessor().getMin(),
                imagePlus.getProcessor().getMax(),
                colorMetadata
        );
    }

}
