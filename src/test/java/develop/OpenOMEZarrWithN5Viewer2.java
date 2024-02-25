package develop;

import bdv.cache.SharedQueue;
import bdv.util.BdvFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.N5ViewerImageData;

import java.io.IOException;
import java.net.URISyntaxException;

public class OpenOMEZarrWithN5Viewer2
{
    public static < T extends NumericType< T > & NativeType< T > > void main( String[] args ) throws IOException, URISyntaxException
    {
        N5ViewerImageData< T > imageData = new N5ViewerImageData<>(
                "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr",
                new SharedQueue( Math.max( 1, Runtime.getRuntime().availableProcessors() / 2 ) )
        );

        BdvFunctions.show(
                imageData.getSourcesAndConverters(),
                imageData.getNumTimepoints(),
                imageData.getBdvOptions());
    }
}
