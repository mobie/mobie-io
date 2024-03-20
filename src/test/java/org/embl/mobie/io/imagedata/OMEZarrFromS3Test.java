package org.embl.mobie.io.imagedata;

import mpicbg.spim.data.sequence.VoxelDimensions;
import org.embl.mobie.io.util.S3Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OMEZarrFromS3Test
{
    @Test
    public void openOMEZarrFromS3()
    {
        N5ImageData< ? > n5ImageData = new N5ImageData<>( "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr" );
        VoxelDimensions voxelDimensions = n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

}