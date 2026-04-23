package examples;

import bdv.cache.SharedQueue;
import bdv.util.Affine3DHelpers;
import bdv.viewer.Source;
import ij.ImagePlus;
import ij.measure.Calibration;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.embl.mobie.io.imagedata.ImageData;

public class OpenOMEZarrAndCovertToImagePlus
{
    public static void main( String[] args )
    {
        System.out.println("openOMEZarrAndConvertToImagePlus");
        String uri = "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr";
        ImageDataFormat imageDataFormat = ImageDataFormat.fromPath( uri );
        SharedQueue sharedQueue = new SharedQueue( 1 );
        ImageData< ? > data = ImageDataOpener.open( uri, imageDataFormat, sharedQueue);

        // Get a handle on the non-volatile (.getA()) of the first image
        Source< ? > source = data.getSourcePair( 0 ).getA();

        // Get the voxelDimensions of the full resolution
        VoxelDimensions voxelDimensions = source.getVoxelDimensions();

        // Get the number of resolution levels
        int numMipmapLevels = source.getNumMipmapLevels();

        // Fetch the voxel data of the lowest resolution at the first time point
        RandomAccessibleInterval< ? > rai = source.getSource( 0, numMipmapLevels - 1 );

        // Fetch the corresponding scale
        AffineTransform3D affineTransform = new AffineTransform3D();
        source.getSourceTransform( 0, numMipmapLevels - 1, affineTransform );
        double[] scales = new double[ 3 ];
        for(int d = 0; d < 3; ++d)
            scales[ d ] = Affine3DHelpers.extractScale( affineTransform, d );

        // Translation is in world units; ImageJ origin is stored in pixel units.
        double[] translation = new double[ 3 ];
        for ( int d = 0; d < 3; ++d )
            translation[ d ] = affineTransform.get( d, 3 );

        // Wrap it into an ImagePlus and set the dimensions
        ImagePlus imp = ImageJFunctions.wrap( ( RandomAccessibleInterval ) rai, "image" );
        imp.setDimensions( 1, ( int ) rai.dimension( 2 ), 1 );

        // Create the calibration (for the corresponding resolution level)
        Calibration calibration = new Calibration();
        calibration.setUnit( voxelDimensions.unit() );
        calibration.pixelWidth = scales[ 0 ];
        calibration.pixelHeight = scales[ 1 ];
        calibration.pixelDepth = scales[ 2 ];

        // Convert world translation to ImageJ's pixel-based origin convention.
        calibration.xOrigin = -translation[ 0 ] / calibration.pixelWidth;
        calibration.yOrigin = -translation[ 1 ] / calibration.pixelHeight;
        calibration.zOrigin = -translation[ 2 ] / calibration.pixelDepth;

        // Attached the calibration to the ImagePlus
        imp.setCalibration( calibration );

        // Show image
        imp.show();
    }

}
