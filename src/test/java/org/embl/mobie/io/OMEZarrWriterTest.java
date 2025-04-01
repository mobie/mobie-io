package org.embl.mobie.io;

import bdv.cache.SharedQueue;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import org.embl.mobie.io.imagedata.BioFormatsImageData;
import org.embl.mobie.io.imagedata.ImageData;
import org.embl.mobie.io.util.IOHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OMEZarrWriterTest
{
    @Test
    public void writeAndReadOMEZarr(@TempDir Path tempDir)
    {
        ImagePlus imp = IJ.createImage( "test", "8-bit ramp", 186, 226, 27 );

        String uri = tempDir.resolve("test.zarr").toString();

        OMEZarrWriter.write( imp,
                uri,
                OMEZarrWriter.ImageType.Intensities,
                false );

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
                false );

        String omeXmlPath = IOHelper.combinePath( uri, "OME", "METADATA.ome.xml" );
        assertTrue( new File( omeXmlPath ).exists() );
        assertTrue( isXml( omeXmlPath ) );
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