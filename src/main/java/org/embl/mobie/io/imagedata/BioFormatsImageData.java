package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.OpenersToSpimData;
import ch.epfl.biop.bdv.img.bioformats.BioFormatsHelper;
import ch.epfl.biop.bdv.img.opener.OpenerSettings;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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

                String lowerCaseUri = uri.toLowerCase();

                boolean usePixelUnits =
                        (          lowerCaseUri.endsWith( ".png" )
                                || lowerCaseUri.endsWith( ".jpg" )
                                || lowerCaseUri.endsWith( ".jpeg" )
                                || lowerCaseUri.endsWith( ".bmp" )
                                || lowerCaseUri.endsWith( ".gif" )
                        )
                        ? true : false;

                List< OpenerSettings > settingsList = new ArrayList<>();
                int numSeries = BioFormatsHelper.getNSeries(file);

                for (int i = 0; i < numSeries; i++) {
                    OpenerSettings settings = OpenerSettings.BioFormats()
                            .location( file )
                            .setSerie( i )
                            .useBFMemo( false );

                    // Throws NPE, https://github.com/mobie/mobie-viewer-fiji/issues/1247
                    // if ( usePixelUnits )
                    //    settings.unit( "pixel" );

                    settingsList.add( settings );
                }

                AbstractSpimData< ? > spimData = OpenersToSpimData.getSpimData( settingsList );

                // Since the above throws a NPE, we resort to
                if ( usePixelUnits )
                    setAllVoxelUnitsToPixel( spimData );

                return spimData;
            }
        } );
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }

    private static void setAllVoxelUnitsToPixel(AbstractSpimData<?> spimData) {

        spimData.getSequenceDescription().getViewSetupsOrdered().forEach(setup -> {
            final Method setVoxelSize;
            try
            {
                setVoxelSize = BasicViewSetup.class.getDeclaredMethod("setVoxelSize", VoxelDimensions.class);
            }
            catch ( NoSuchMethodException ex )
            {
                throw new RuntimeException( ex );
            }
            setVoxelSize.setAccessible(true);

            try {
                final double[] dims = new double[setup.getVoxelSize().numDimensions()];
                Arrays.fill(dims,1.0);
                setVoxelSize.invoke(setup, new FinalVoxelDimensions("pixel", dims));
            }
            catch ( Exception e )
            {
                throw new RuntimeException("Failed to set voxel size for setup " + setup.getId(), e);
            }
        });
    }
}
