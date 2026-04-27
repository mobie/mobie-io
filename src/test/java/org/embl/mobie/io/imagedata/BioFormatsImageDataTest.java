package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BioFormatsImageDataTest
{
    @Test
    public void openPNG()
    {
        // https://github.com/mobie/mobie-viewer-fiji/issues/1247
        ImageData< ? > imageData = ImageDataOpener.open(
                "src/test/resources/images/boats.png",
                ImageDataFormat.fromPath( "src/test/resources/images/boats.png" ),
                new SharedQueue( 1 ) );
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertEquals( "pixel", voxelDimensions.unit() );
    }

    @Test
    public void openIlastikSavedByIlastik()
    {
        ImageData< ? > imageData = ImageDataOpener.open(
                "src/test/resources/ilastik/probabilities-from-ilastik.h5",
                ImageDataFormat.fromPath( "src/test/resources/ilastik/probabilities-from-ilastik.h5" ),
                new SharedQueue( 1 ));
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

    public static void main( String[] args )
    {
        new BioFormatsImageDataTest().openPNG();
    }

}