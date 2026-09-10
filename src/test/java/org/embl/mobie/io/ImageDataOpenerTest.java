package org.embl.mobie.io;

import bdv.cache.SharedQueue;
import org.embl.mobie.io.imagedata.ImageData;
import org.embl.mobie.io.imagedata.N5ImageData;
import org.embl.mobie.io.imagedata.PyramidalZarrJavaImageData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageDataOpenerTest
{
    private ImageDataOpener.ZarrOpener originalOpener;

    @BeforeEach
    void rememberOriginalOpener()
    {
        originalOpener = ImageDataOpener.getZarrOpener();
    }

    @AfterEach
    void restoreOriginalOpener()
    {
        ImageDataOpener.setZarrOpener( originalOpener );
    }

    @Test
    void autoDetectFindsZarrJavaBackend()
    {
        assertDoesNotThrow( () -> Class.forName( "ome.zarr.zarrjava.ZarrJavaPyramidBackend" ) );

        ImageDataOpener.autoConfigureZarrOpener();

        assertEquals( ImageDataOpener.ZarrOpener.OME_Zarr_Zarr_Java, ImageDataOpener.getZarrOpener() );
    }

    @Test
    void runtimeOverrideChangesWhichImageDataImplementationIsOpened()
    {
        String uri = new File( "src/test/resources/images/blobs.ome.zarr" ).getAbsolutePath();
        ImageDataFormat imageDataFormat = ImageDataFormat.fromPath( uri );
        SharedQueue sharedQueue = new SharedQueue( 1 );

        assertEquals( ImageDataFormat.OmeZarr, imageDataFormat );

        ImageDataOpener.setZarrOpener( ImageDataOpener.ZarrOpener.MOBIE_N5 );
        ImageData< ? > n5ImageData = ImageDataOpener.open( uri, imageDataFormat, sharedQueue );
        assertTrue( n5ImageData instanceof N5ImageData );

        ImageDataOpener.setZarrOpener( ImageDataOpener.ZarrOpener.OME_Zarr_Zarr_Java );
        ImageData< ? > zarrJavaImageData = ImageDataOpener.open( uri, imageDataFormat, sharedQueue );
        assertTrue( zarrJavaImageData instanceof PyramidalZarrJavaImageData );
    }
}

