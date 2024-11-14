package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IlastikImageDataTest
{
    @Test
    public void openIlastikSavedByFiji()
    {
        ImageData< ? > imageData = ImageDataOpener.open(
                "src/test/resources/ilastik/from-fiji.h5",
                ImageDataFormat.fromPath( "src/test/resources/ilastik/from-fiji.h5" ),
                new SharedQueue( 1 ) );
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
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

}