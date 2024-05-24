package develop;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import org.embl.mobie.io.imagedata.N5ImageData;

public class OpenLabelsFromOMEZarr
{
    public static void main( String[] args )
    {
        //String uri = "/Users/tischer/Downloads/20200812-CardiomyocyteDifferentiation14-Cycle1_mip.zarr/B/03/0";
        String uri = "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr";
        N5ImageData< ? > imageData = new N5ImageData<>( uri );
        int numDatasets = imageData.getNumDatasets();
        System.out.println("Dataset names:");
        for ( int i = 0; i < numDatasets; i++ )
            System.out.println( imageData.getName( i ) );

        //BdvFunctions.show( imageData.getSourcesAndConverters(), 1, BdvOptions.options() );
    }
}
