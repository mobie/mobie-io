package org.embl.mobie.io;

import bdv.cache.SharedQueue;
import ij.IJ;
import ij.ImagePlus;
import org.embl.mobie.io.imagedata.ImageData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
}