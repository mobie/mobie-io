package org.embl.mobie.io;

import bdv.cache.SharedQueue;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import org.embl.mobie.io.imagedata.ImageData;
import org.embl.mobie.io.util.IOHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

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

    @Test
    public void writeZarrV3WithSharding(@TempDir Path tempDir) throws IOException
    {
        ImagePlus imp = IJ.createImage( "test", "8-bit ramp", 128, 128, 16 );

        String uri = tempDir.resolve("test.zarr").toString();

        final PrintStream originalErr = System.err;
        try ( ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
              PrintStream mutedErr = new PrintStream( errBuffer ) )
        {
            // n5-ij currently logs recoverable metadata-write exceptions for sharded Zarr3.
            System.setErr( mutedErr );
            OMEZarrWriter.write(
                    imp,
                    uri,
                    OMEZarrWriter.ImageType.Intensities,
                    new int[]{ 32, 32, 1, 16, 1 },
                    new int[]{ 64, 64, 1, 16, 1 },
                    OMEZarrWriter.StorageFormat.ZARR3,
                    true,
                    null );
        }
        finally
        {
            System.setErr( originalErr );
        }

        final Path root = Paths.get( uri );
        assertTrue( Files.exists( root.resolve( "zarr.json" ) ) );
        assertTrue( hasShardingCodec( root ) );
    }

    private static boolean hasShardingCodec( Path root ) throws IOException
    {
        try ( Stream< Path > paths = Files.walk( root ) )
        {
            return paths
                    .filter( path -> path.getFileName().toString().equals( "zarr.json" ) )
                    .anyMatch( path -> {
                        try
                        {
                            final String json = new String( Files.readAllBytes( path ), StandardCharsets.UTF_8 );
                            return json.contains( "sharding_indexed" ) || json.contains( "ShardingIndexedCodec" );
                        }
                        catch ( IOException e )
                        {
                            throw new RuntimeException( e );
                        }
                    } );
        }
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