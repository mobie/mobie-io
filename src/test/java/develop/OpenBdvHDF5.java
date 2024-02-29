package develop;

import bdv.cache.SharedQueue;
import bdv.util.BdvFunctions;
import bdv.viewer.Source;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import org.embl.mobie.io.imagedata.BDVXMLImageData;

public class OpenBdvHDF5
{
    public static < T extends NumericType< T > & NativeType< T > > void main( String[] args )
    {
        BDVXMLImageData< T > imageData = new BDVXMLImageData<>(
                "/Users/tischer/Desktop/bdv/mri.xml",
                new SharedQueue( Math.max( 1, Runtime.getRuntime().availableProcessors() / 2 ) )
        );

        Pair< Source< T >, Source< ? extends Volatile< T > > > sourcePair = imageData.getSourcePair( 0, "image" );

        BdvFunctions.show( sourcePair.getB() );
    }
}
