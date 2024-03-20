package org.embl.mobie.io.imagedata;

import mpicbg.spim.data.sequence.VoxelDimensions;
import org.embl.mobie.io.util.S3Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OMEZarrFromS3WithWrongCredentialsTest
{
    @Test
    public void openOMEZarrFromS3WithWrongCredentials()
    {
        assertThrows( RuntimeException.class, () -> {
            S3Utils.setS3AccessAndSecretKey( new String[]{ "4vJRUoUQZix2x7wPRlSy", "wrongSecretKey" } );
            N5ImageData< ? > n5ImageData = new N5ImageData<>( "https://s3.embl.de/mobie-credentials-test/test/images/ome-zarr/8kmont5.ome.zarr" );
            VoxelDimensions voxelDimensions = n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        });
    }
}