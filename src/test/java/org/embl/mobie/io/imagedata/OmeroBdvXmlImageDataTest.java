package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OmeroBdvXmlImageDataTest
{
    @Test
    public void openOmeroBdvXml()
    {
        System.out.println("openOmeroBdvXml...");
        String uri = new File( "src/test/resources/omero-bdv.xml" ).getAbsolutePath();
        BDVXMLImageData< ? > imageData = new BDVXMLImageData<>( uri, new SharedQueue( 1 ) );
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
        System.out.println("...done!");
    }

    public static void main( String[] args )
    {
        new OmeroBdvXmlImageDataTest().openOmeroBdvXml();
    }

}