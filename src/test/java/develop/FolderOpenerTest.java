package develop;

import bdv.cache.SharedQueue;
import bdv.util.BdvFunctions;
import bdv.viewer.Source;
import ij.ImagePlus;
import ij.plugin.FolderOpener;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.embl.mobie.io.imagedata.ImageData;
import org.embl.mobie.io.imagedata.TIFFImageData;

import java.io.File;
import java.util.Arrays;

public class FolderOpenerTest
{
    public static < T extends NumericType< T > & NativeType< T > > void main( String[] args )
    {

        String path = "/Users/tischer/Documents/mobie-viewer-fiji/src/test/resources/collections/mri-stack/";

        path = "/Volumes/emcf/ronchi/MRC-MM/aligned/G0";
//        ImagePlus imp = FolderOpener.open(
//                path,
//                "virtual filter=(.*.tif.*)");
//        imp.show();

        ImageData< T > imageData = ImageDataOpener.open( path );
        Pair< Source< T >, Source< ? extends Volatile< T > > > sourcePair = imageData.getSourcePair( 0 );

        BdvFunctions.show( sourcePair.getB() );

//        boolean directory = new File( "https://sgsdfgdsf/sfsgd" ).isDirectory();
//        TIFFImageData< ? > imageData = new TIFFImageData<>( path, new SharedQueue( 1 ) );
//        Source< ? > source = imageData.getSourcePair( 0 ).getA();
//        System.out.println( Arrays.toString( source.getSource( 0,0 ).dimensionsAsLongArray() ) );
    }
}
