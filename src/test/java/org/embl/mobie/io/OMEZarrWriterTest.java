package org.embl.mobie.io;

import bdv.cache.SharedQueue;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import org.embl.mobie.io.imagedata.ImageData;
import org.embl.mobie.io.util.ChunkSizeComputer;
import org.embl.mobie.io.util.IOHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OMEZarrWriterTest
{
    @Test
    public void writeAndReadOMEZarr(@TempDir Path tempDir)
    {
        ImagePlus imp = IJ.createImage( "test", "8-bit ramp", 186, 226, 27 );

        String uri = tempDir.resolve("test.zarr").toString();

        OMEZarrWriter.write(
                imp,
                uri,
                OMEZarrWriter.ImageType.Intensities,
                true );

        ImageData< ? > imageData = ImageDataOpener.open(
                uri,
                ImageDataFormat.fromPath( uri ),
                new SharedQueue( 1 ) );

        long dim0 = imageData.getSourcePair( 0 ).getB()
                .getSource( 0, 0 ).dimension( 0 );

        assertEquals( 186, dim0 );
    }

    @Test
    public void writeOMEZarrWithOMEMetadata(@TempDir Path tempDir)
    {
        ImagePlus imp = IOHelper.openWithBioFormats( "src/test/resources/images/test.tif", 0 );

        String uri = tempDir.resolve("test.zarr").toString();

        OMEZarrWriter.write( imp,
                uri,
                OMEZarrWriter.ImageType.Intensities,
                true );

        String omeXmlPath = IOHelper.combinePath( uri, "OME", "METADATA.ome.xml" );
        assertTrue( new File( omeXmlPath ).exists() );
        assertTrue( isXml( omeXmlPath ) );
    }


    //@Test
    // TODO: This test does not run in GitHub actions, because
    //  ChannelSplitter.split( imp ) internally calls
    //  IJ.run(imp2, "Grays", "");
    //  , which requires a X11 DISPLAY variable to be set
    public void omeMetadataForSplitImage( @TempDir Path tempDir)
    {
        // Ensure that we don't write wrong metadata
        // when users add a single channel of a multi-channel image
        // For details see: https://forum.image.sc/t/create-ome-xml-when-creating-ome-zarr-in-fiji/110683/13
        ImagePlus imp = IOHelper.openWithBioFormats( "src/test/resources/images/xyc_xy__two_images.lif", 0 );
        String omeXml = IOHelper.getOMEXml( imp );

        ImagePlus[] channels = ChannelSplitter.split( imp );
        String channelOmeXml = IOHelper.getOMEXml( channels[ 0 ] );

        assertNotNull( omeXml );
        assertNull( channelOmeXml );
    }

    @Test
    public void omeMetadataForCroppedImage( @TempDir Path tempDir)
    {
        // Ensure that we don't write wrong metadata
        // when users crop an image
        // For details see: https://forum.image.sc/t/create-ome-xml-when-creating-ome-zarr-in-fiji/110683/13
        ImagePlus imp = IOHelper.openWithBioFormats( "src/test/resources/images/xyc_xy__two_images.lif", 0 );
        String omeXml = IOHelper.getOMEXml( imp );
        assertTrue( IOHelper.checkMetadataConsistency( imp, omeXml ) );

        imp = imp.resize(400, 400, 1, "bilinear");
        assertFalse( IOHelper.checkMetadataConsistency( imp, omeXml ) );

        String uri = tempDir.resolve("test.zarr").toString();
        OMEZarrWriter.write( imp,
                uri,
                OMEZarrWriter.ImageType.Intensities,
                false );

        String omeXmlPath = IOHelper.combinePath( uri, "OME", "METADATA.ome.xml" );
        assertFalse( new File( omeXmlPath ).exists() );
    }

    private static boolean isXml( String filePath )
    {
        try {
            File file = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}