package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BDVXMLImageDataTest
{
    @Test
    public void openPlatybrowserBDVXMLN5()
    {
        BDVXMLImageData< ? > imageData = new BDVXMLImageData<>( "https://raw.githubusercontent.com/mobie/platybrowser-project/main/data/1.0.1/images/remote/sbem-6dpf-1-whole-raw.xml", new SharedQueue( 1 ) );
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
    }

}