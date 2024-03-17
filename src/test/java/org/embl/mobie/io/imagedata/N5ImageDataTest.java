package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class N5ImageDataTest
{
    @Test
    public void openOMEZarrFromS3()
    {
        N5ImageData< ? > n5ImageData = new N5ImageData<>( "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr" );
        VoxelDimensions voxelDimensions = n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

}