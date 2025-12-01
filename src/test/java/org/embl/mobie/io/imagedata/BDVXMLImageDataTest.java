package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class BDVXMLImageDataTest
{
    @Test
    public void openPlatybrowserBDVXMLN5()
    {
        ImageData< ? > imageData = new BDVXMLImageData<>( "https://raw.githubusercontent.com/mobie/platybrowser-project/main/data/1.0.1/images/remote/sbem-6dpf-1-whole-raw.xml", new SharedQueue( 1 ) );
        int numDatasets = imageData.getNumDatasets();
        for ( int datasetIndex = 0; datasetIndex < numDatasets; datasetIndex++ )
        {
            System.out.println("Dataset index: " + datasetIndex );
            System.out.println("Name: " + imageData.getName( datasetIndex ) );
            //System.out.println("Color: " + imageData.getMetadata( datasetIndex ).getColor() );
        }
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

    public static void main( String[] args )
    {
        new BDVXMLImageDataTest().openPlatybrowserBDVXMLN5();
    }

}