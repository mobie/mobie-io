package examples;

import bdv.cache.SharedQueue;
import bdv.util.BdvFunctions;
import org.embl.mobie.io.imagedata.N5ImageData;

public class OpenOMEZarrExample
{
    public static void main( String[] args )
    {
        //String uri = "https://s3.embl.de/imatrec/IMATREC_HiTT_20240414_AS/TAL_20to200_20230627_NA_01_epo_05.ome.zarr";
        String uri = "https://livingobjects.ebi.ac.uk/idr/zarr/v0.5/idr0033A/BR00109990_C2.zarr/0/"; // v5 OME-Zarr

        long start = System.currentTimeMillis();
        N5ImageData< ? > imageData = new N5ImageData<>(
                uri,
                new SharedQueue( Math.max( 1, Runtime.getRuntime().availableProcessors() / 2 ) )
        );
        imageData.getSourcesAndConverters(); // triggers actual opening of the data
        System.out.println("Opened " + uri );
        System.out.println("Opening time [ms]: " + (System.currentTimeMillis() - start ) );

        BdvFunctions.show(
                imageData.getSourcesAndConverters(),
                imageData.getNumTimepoints(),
                imageData.getBdvOptions());
    }
}
