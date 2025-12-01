package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.iterableinterval.unary.MinMax;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.ValuePair;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class ImageDataTest < R extends RealType< R > >
{
    @Test
    public void openOmeroBdvXml() throws ExecutionException, InterruptedException
    {
        // References
        // https://github.com/mobie/mobie-io/issues/169
        // https://forum.image.sc/t/opening-omero-datasets-in-mobie/117612/22

        ImageJ imageJ = new ImageJ();
        imageJ.command().run(ch.epfl.biop.bdv.img.omero.command.OmeroConnectCommand.class, true,
                "host", "omero-tim.gerbi-gmb.de",
                "username", "read-tim",
                "password", "read-tim"
        ).get();

        String uri = new File( "src/test/resources/images/omero-bdv.xml" ).getAbsolutePath();
        BDVXMLImageData< ? > imageData = new BDVXMLImageData<>( uri, new SharedQueue( 1 ) );
        System.out.println( "Number of datasets: " + imageData.getNumDatasets() );
        for ( int datasetIndex = 0; datasetIndex < imageData.getNumDatasets(); datasetIndex++ )
        {
            System.out.println( "Dataset index: " + datasetIndex );
            System.out.println( "  Name: " + imageData.getName( datasetIndex ) );
            System.out.println( "  Color: " + imageData.getMetadata( datasetIndex ).getColor() );
            System.out.println( "  Contrast limits: " + imageData.getMetadata( datasetIndex ).minIntensity() + ", " + imageData.getMetadata( datasetIndex ).maxIntensity() );
            System.out.println( "  Voxel dimensions: " + imageData.getSourcePair( datasetIndex ).getB().getVoxelDimensions() );
        }

        VoxelDimensions voxelDimensions = imageData.getSourcePair( 0 ).getB().getVoxelDimensions();
        assertEquals( 0.6443438334464736D, voxelDimensions.dimension( 0 ), 0.01  );
        assertEquals( "Slide_00.vsi [10x_09]-FL FITC", imageData.getName( 7 ) );
        assertEquals( 1000, imageData.getMetadata( 10 ).maxIntensity() );

        imageJ.command().run(ch.epfl.biop.bdv.img.omero.command.OmeroDisconnectCommand .class, true,
                "host", "omero-tim.gerbi-gmb.de"
        ).get();

        System.out.println("Done!");
    }

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

    private static < R extends RealType< R > > ValuePair< R, R > computeMinMax( ImageData< ? > imageData )
    {
//        RandomAccessibleInterval< R > vRai = ( RandomAccessibleInterval< R > ) imageData.getSourcePair( 0 ).getB().getSource( 0, 0 );
//        MinMax< R > vMinMax = new MinMax<>();
//        ValuePair< R, R > vValuePair = vMinMax.compute( vRai );
//        vValuePair = vMinMax.compute( vRai );

        RandomAccessibleInterval< R > rai = ( RandomAccessibleInterval< R > ) imageData.getSourcePair( 0 ).getA().getSource( 0, 0 );
        MinMax< R > minMax = new MinMax<>();
        ValuePair< R, R > valuePair = minMax.compute( rai );

        return valuePair;
    }

    public static void main( String[] args ) throws ExecutionException, InterruptedException
    {
        //new ImageDataTest().openOmeroBdvXml();
        new ImageDataTest().openMRC();
    }
}