package org.embl.mobie.io;

import ij.IJ;
import ij.ImagePlus;
import org.embl.mobie.io.imagedata.ImageData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OMEZarrWriterTest
{
    @Test
    public void writeAndReadOMEZarr()
    {
        ImagePlus imp = IJ.createImage( "test", "8-bit ramp", 186, 226, 27 );

        String uri = "src/test/tmp/test.zarr";

        OMEZarrWriter.write( imp,
                uri,
                OMEZarrWriter.ImageType.Intensities,
                true );

        ImageData< ? > imageData = ImageDataOpener.open( uri );

        long dim0 = imageData.getSourcePair( 0 ).getB()
                .getSource( 0, 0 ).dimension( 0 );

        assertEquals( 186, dim0 );
    }
}