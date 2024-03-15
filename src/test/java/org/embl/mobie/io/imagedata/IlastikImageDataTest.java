package org.embl.mobie.io.imagedata;

import mpicbg.spim.data.sequence.VoxelDimensions;
import org.embl.mobie.io.ImageDataOpener;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IlastikImageDataTest
{
    @Test
    public void openDatasetFromFiji()
    {
        ImageData< ? > imageData = ImageDataOpener.open( "src/test/resources/ilastik/from-fiji.h5" );
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

    @Test
    public void openDatasetFromIlastik()
    {
        ImageData< ? > imageData = ImageDataOpener.open( "src/test/resources/ilastik/probabilities-from-ilastik.h5" );
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

}