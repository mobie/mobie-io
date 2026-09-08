package org.embl.mobie.io.imagedata;

import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccessibleInterval;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PyramidalN5ImageDataTest
{
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
        assertEquals( 2, numDatasets); // EM and segmentation labels
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