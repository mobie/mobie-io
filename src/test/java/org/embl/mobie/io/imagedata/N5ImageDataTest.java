package org.embl.mobie.io.imagedata;

import mpicbg.spim.data.sequence.VoxelDimensions;
import org.embl.mobie.io.util.S3Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class N5ImageDataTest
{
    @Test
    public void openOMEZarrFromEMBLHDS3()
    {
        N5ImageData< ? > n5ImageData = new N5ImageData<>( "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr" );
        VoxelDimensions voxelDimensions = n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

    @Test
    public void openOMEZarrFromEMBLEBIS3()
    {
        // FIXME: Does not work from within "https://github.com/mobie/mouse-embryo-spatial-transcriptomics-project"
        N5ImageData< ? > n5ImageData = new N5ImageData<>( "https://uk1s3.embassy.ebi.ac.uk/idr/zarr/v0.4/idr0138A/TimEmbryos-120919/HybCycle_29/MMStack_Pos0.ome.zarr" );
        int numDatasets = n5ImageData.getNumDatasets();
        VoxelDimensions voxelDimensions = n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

//    @Test
//    public void openOMEZarrFromS3WithCredentials()
//    {
//        S3Utils.setS3AccessAndSecretKey( new String[]{ "", "" } );
//        N5ImageData< ? > n5ImageData = new N5ImageData<>( "https://s3.embl.de/mobie-credentials-test/???.ome.zarr" );
//        VoxelDimensions voxelDimensions = n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions();
//        assertNotNull( voxelDimensions );
//    }

}