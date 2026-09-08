package org.embl.mobie.io.imagedata;

import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccessibleInterval;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PyramidalN5ImageDataTest
{
    @Test
    public void openLocalOMEZarr()
    {
        // Checks whether space characters in the path are tolerated
        System.out.println("openLocalOMEZarr");

        String path = new File( "src/test/resources/images/blobs.ome.zarr" ).toString();
        try
        {
            PyramidalN5ImageData< ? > n5ImageData = new PyramidalN5ImageData<>( path );
            assertNotNull( n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions() );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    @Test
    public void openLocalOMEZarrWithSpacesInThePath()
    {
        // Checks whether space characters in the path are tolerated
        System.out.println("openLocalOMEZarrWithSpaces");

        String path = new File( "src/test/resources/images/blobs space.ome.zarr" ).toString();
        try
        {
            PyramidalZarrJavaImageData< ? > n5ImageData = new PyramidalZarrJavaImageData<>( path );
            assertNotNull( n5ImageData.getSourcePair( 0 ).getB().getVoxelDimensions() );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    @Test
    public void openOMEZarr3FromS3()
    {
        System.out.println( "openOMEZarr3FromS3" );

        PyramidalN5ImageData< ? > imageData = new PyramidalN5ImageData<>( "https://livingobjects.ebi.ac.uk/idr/zarr/v0.5/idr0033A/BR00109990_C2.zarr/0/" );
        int numDatasets = imageData.getNumDatasets();
        List< ? extends SourceAndConverter< ? > > sourcesAndConverters = imageData.getSourcesAndConverters();
        assertEquals( numDatasets, 5 ); // 5 channels
    }

    @Test
    public void openOMEZarr2FromS3()
    {
        System.out.println( "openOMEZarr2FromS3" );

        PyramidalN5ImageData< ? > imageData = new PyramidalN5ImageData<>( "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr" );
        int numDatasets = imageData.getNumDatasets();
        List< ? extends SourceAndConverter< ? > > sourcesAndConverters = imageData.getSourcesAndConverters();
        assertEquals( 1, numDatasets); // EM only
        RandomAccessibleInterval< ? > source = imageData.getSourcePair( 0 ).getA().getSource( 0, 0 );
        long[] maxAsLongArray = source.maxAsLongArray();
        Object pixelValue = source.getAt( 0, 0, 0 );
        System.out.println( "maxAsLongArray: " + Arrays.toString( maxAsLongArray ) );
        System.out.println();
    }

    @Test
    public void openLabelsOMEZarr2FromS3()
    {
        System.out.println( "openLabelsOMEZarr2FromS3" );

        PyramidalZarrJavaImageData< ? > imageData = new PyramidalZarrJavaImageData<>( "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr/labels/cells" );
        int numDatasets = imageData.getNumDatasets();
        List< ? extends SourceAndConverter< ? > > sourcesAndConverters = imageData.getSourcesAndConverters();
        assertEquals( 1, numDatasets); // labels only
        RandomAccessibleInterval< ? > source = imageData.getSourcePair( 0 ).getA().getSource( 0, 0 );
        long[] maxAsLongArray = source.maxAsLongArray();
        Object pixelValue = source.getAt( 0, 0, 0 );
        System.out.println( "maxAsLongArray: " + Arrays.toString( maxAsLongArray ) );
        System.out.println();
    }

}