package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageStatistics;
import ij.process.LUT;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.janelia.saalfeldlab.n5.universe.metadata.RGBAColorMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;

import java.io.IOException;

import static ij.measure.Measurements.MIN_MAX;

public class ImagePlusImageData< T extends NumericType< T > & NativeType< T > > extends SpimDataImageData< T >
{
    private final ImagePlus imagePlus;

    public ImagePlusImageData( ImagePlus imagePlus, SharedQueue sharedQueue )
    {
        super( new SpimDataOpener()
        {
            @Override
            public AbstractSpimData open( String uri ) throws IOException
            {
                return ImagePlusToSpimData.getSpimData( imagePlus );
            }
        } );

        this.imagePlus = imagePlus;
        this.sharedQueue = sharedQueue;
    }

    @Override
    public CanonicalDatasetMetadata getMetadata( int datasetIndex )
    {
        if ( ! isOpen ) open( spimDataOpener, uri );

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
