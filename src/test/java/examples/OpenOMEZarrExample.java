package examples;

import bdv.cache.SharedQueue;
import bdv.util.BdvFunctions;
import bdv.viewer.SourceAndConverter;
import org.embl.mobie.io.imagedata.N5ImageData;
import org.jruby.RubyProcess;

import java.util.List;

public class OpenOMEZarrExample
{
    public static void main( String[] args )
    {
        //String uri = "https://s3.embl.de/imatrec/IMATREC_HiTT_20240414_AS/TAL_20to200_20230627_NA_01_epo_05.ome.zarr";
        String uri = "/Users/tischer/Downloads/20240524_1_s2.zarr";

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
