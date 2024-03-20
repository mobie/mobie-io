package org.embl.mobie.io.imagedata;

import mpicbg.spim.data.sequence.VoxelDimensions;
import org.embl.mobie.io.util.S3Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OMEZarrFromEBIS3
{
    @Test
    public void openOMEZarrFromEBIS3()
    {
        // FIXME: Does not work from within "https://github.com/mobie/mouse-embryo-spatial-transcriptomics-project"
        N5ImageData< ? > n5ImageData = new N5ImageData<>( "https://uk1s3.embassy.ebi.ac.uk/idr/zarr/v0.4/idr0138A/TimEmbryos-120919/HybCycle_29/MMStack_Pos0.ome.zarr" );
        int numDatasets = n5ImageData.getNumDatasets();
        VoxelDimensions voxelDimensions = n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

}