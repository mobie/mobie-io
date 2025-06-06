package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import bdv.util.BdvFunctions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.iterableinterval.unary.MinMax;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.ValuePair;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ImageDataTest < R extends RealType< R > >
{
    @Test
    public void openPNG()
    {
        System.out.println("openPNG...");
        String uri = new File( "src/test/resources/images/boats.png" ).toString();
        ImageDataFormat imageDataFormat = ImageDataFormat.fromPath( uri );
        ImageData< ? > imageData = ImageDataOpener.open( uri, imageDataFormat, new SharedQueue( 1 ) );
        ValuePair< R, R > valuePair = computeMinMax( imageData );
        assertEquals( 3, valuePair.getA().getRealDouble() );
        assertEquals( 220, valuePair.getB().getRealDouble() );
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertNotNull( voxelDimensions );
        System.out.println("...openPNG: Done!");
    }

    @Test
    public void openMRC()
    {
        System.out.println("openMRC");
        String uri = new File( "src/test/resources/images/gridmap_stiched_3_bin8.mrc" ).toString();
        ImageDataFormat imageDataFormat = ImageDataFormat.fromPath( uri );
        ImageData< ? > imageData = ImageDataOpener.open( uri, imageDataFormat, new SharedQueue( 1 ) );
        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        //BdvFunctions.show( imageData.getSourcePair( 0 ).getB() );
        ValuePair< R, R > valuePair = computeMinMax( imageData );
        assertEquals( -1468, valuePair.getA().getRealDouble() );
        assertEquals( 9827, valuePair.getB().getRealDouble() );
        assertNotNull( voxelDimensions );
        System.out.println("...openMRC: Done!");
    }

    public static void main( String[] args )
    {
        ExecutorService exec = Executors.newCachedThreadPool();
        //exec.submit(() -> {new ImageDataTest().openPNG();});
        exec.submit(() -> {new ImageDataTest().openMRC();});
    }

    private static < R extends RealType< R > > ValuePair< R, R > computeMinMax( ImageData< ? > imageData )
    {
        RandomAccessibleInterval< R > vRai = ( RandomAccessibleInterval< R > ) imageData.getSourcePair( 0 ).getB().getSource( 0, 0 );
        MinMax< R > vMinMax = new MinMax<>();
        ValuePair< R, R > vValuePair = vMinMax.compute( vRai );
        vValuePair = vMinMax.compute( vRai );

        RandomAccessibleInterval< R > rai = ( RandomAccessibleInterval< R > ) imageData.getSourcePair( 0 ).getA().getSource( 0, 0 );
        MinMax< R > minMax = new MinMax<>();
        ValuePair< R, R > valuePair = vMinMax.compute( rai );

        return valuePair;
    }

}