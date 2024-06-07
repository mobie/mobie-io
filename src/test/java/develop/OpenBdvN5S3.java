package develop;

import bdv.cache.SharedQueue;
import bdv.util.BdvFunctions;
import bdv.viewer.Source;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.embl.mobie.io.imagedata.BDVXMLImageData;
import org.embl.mobie.io.imagedata.ImageData;

public class OpenBdvN5S3
{
    public static < T extends NumericType< T > & NativeType< T > > void main( String[] args )
    {
        ImageData< T > imageData = ImageDataOpener.open(
                "https://raw.githubusercontent.com/mobie/platybrowser-project/main/data/1.0.1/images/remote/sbem-6dpf-1-whole-raw.xml",
                ImageDataFormat.BdvN5S3,
                new SharedQueue( 1 ) );

        Pair< Source< T >, Source< ? extends Volatile< T > > > sourcePair = imageData.getSourcePair( 0 );

        BdvFunctions.show( sourcePair.getB() );
    }
}
