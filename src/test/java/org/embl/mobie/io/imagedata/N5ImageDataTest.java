package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import bdv.viewer.SourceAndConverter;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class N5ImageDataTest
{
    @Test
    public void openOMEZarrFromS3()
    {
        System.out.println("openOMEZarrFromS3");
        N5ImageData< ? > n5ImageData = new N5ImageData<>( "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr" );
        int numDatasets = n5ImageData.getNumDatasets();
        List< ? extends SourceAndConverter< ? > > sourcesAndConverters = n5ImageData.getSourcesAndConverters();
        assertEquals( numDatasets, 2 ); // EM and segmentation labels
        VoxelDimensions voxelDimensions = n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

    @Test
    public void openOMEZarrFromEBIS3()
    {
        System.out.println("openOMEZarrFromEBIS3");
        N5ImageData< ? > n5ImageData = new N5ImageData<>( "https://uk1s3.embassy.ebi.ac.uk/idr/zarr/v0.4/idr0138A/TimEmbryos-120919/HybCycle_29/MMStack_Pos0.ome.zarr" );
        VoxelDimensions voxelDimensions = n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

    @Test
    public void openOMEZarrFromS3WithCredentialsWithN5ImageData()
    {
        System.out.println("openOMEZarrFromS3WithCredentials");
        N5ImageData< ? > n5ImageData = new N5ImageData<>(
                "https://s3.embl.de/mobie-credentials-test/test/images/ome-zarr/8kmont5.ome.zarr",
                new String[]{
                        "MHLAyeu3fyBAx3egnzOE",
                        "Dj9SCkDr3XtWEohBKxCcWFZgU0YkpxRK7TndQYmm"
                });
        VoxelDimensions voxelDimensions = n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

    @Test
    public void openOMEZarrFromS3WithCredentialsWithImageDataOpener()
    {
        // This test uses the ImageDataOpener ( instead of directly N5ImageData )
        System.out.println("openOMEZarrFromS3WithCredentialsV2");
        ImageDataFormat imageDataFormat = ImageDataFormat.OmeZarrS3;
        imageDataFormat.setS3SecretAndAccessKey(
                new String[]{
                        "MHLAyeu3fyBAx3egnzOE",
                        "Dj9SCkDr3XtWEohBKxCcWFZgU0YkpxRK7TndQYmm"
                } );
        ImageData< ? > imageData = ImageDataOpener.open(
                "https://s3.embl.de/mobie-credentials-test/test/images/ome-zarr/8kmont5.ome.zarr",
                imageDataFormat,
                new SharedQueue( 1 ) );
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

    @Test
    public void openOMEZarrFromS3WithWrongCredentials()
    {
        System.out.println("openOMEZarrFromS3WithWrongCredentials");

        try
        {
            N5ImageData< ? > n5ImageData = new N5ImageData<>(
                    "https://s3.embl.de/mobie-credentials-test/test/images/ome-zarr/8kmont5.ome.zarr",
                    new String[]{ "4vJRUoUQZix2x7wPRlSy", "wrongSecretKey" });
            assertNotNull( n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions() );
            System.out.println("Succeeded incorrectly; this should fail.");
            fail();
        }
        catch ( Exception e )
        {
            System.out.println("Failed correctly due to wrong credentials.");
            assertTrue( true );
        }
    }

    public static void main( String[] args )
    {
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.submit(() -> {new N5ImageDataTest().openOMEZarrFromS3();});
        exec.submit(() -> {new N5ImageDataTest().openOMEZarrFromEBIS3();});
        //exec.submit(() -> {new N5ImageDataTest().openOMEZarrFromS3WithCredentialsWithN5ImageData();});
        exec.submit(() -> {new N5ImageDataTest().openOMEZarrFromS3WithWrongCredentials();});
    }
}