package develop;

import bdv.cache.SharedQueue;
import bdv.util.BdvFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.imagedata.N5ImageData;

import java.io.IOException;
import java.net.URISyntaxException;

public class OpenOMEZarrAsN5ImageData
{
    public static < T extends NumericType< T > & NativeType< T > > void main( String[] args ) throws IOException, URISyntaxException
    {
        String uri = "https://s3.embl.de/imatrec/IMATREC_HiTT_20240414_AS/TAL_20to200_20230627_NA_01_epo_05.ome.zarr";

        N5ImageData< T > imageData = new N5ImageData<>(
                uri,
                new SharedQueue( Math.max( 1, Runtime.getRuntime().availableProcessors() / 2 ) )
        );

        BdvFunctions.show(
                imageData.getSourcesAndConverters(),
                imageData.getNumTimepoints(),
                imageData.getBdvOptions());
    }
}
