package spimdata;

import bdv.img.cache.VolatileCachedCellImg;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import net.imglib2.RandomAccess;
import net.imglib2.img.cell.CellLocalizingCursor;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.GenericByteType;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;
import org.embl.mobie.io.n5.util.DownsampleBlock;
import org.embl.mobie.io.n5.writers.WriteImgPlusToN5;
import org.embl.mobie.io.ome.zarr.writers.imgplus.WriteImgPlusToN5BdvOmeZarr;
import org.embl.mobie.io.ome.zarr.writers.imgplus.WriteImgPlusToN5OmeZarr;
import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.GzipCompression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OmeZarrWithWriterTest {

    private String imageName;
    private AffineTransform3D sourceTransform;
    private File tempDir;
    private DownsampleBlock.DownsamplingMethod downsamplingMethod;
    private Compression compression;

    @BeforeEach
    void setUp( @TempDir Path tempDir ) throws IOException {
        this.tempDir = tempDir.toFile();
        imageName = "testImage";
        sourceTransform = new AffineTransform3D();
        downsamplingMethod = DownsampleBlock.DownsamplingMethod.Average;
        compression = new GzipCompression();
    }

    ImagePlus makeImage( String imageName, int width, int height, int depth ) {
        // make an image with random values
        return IJ.createImage(imageName, "8-bit noise", width, height, depth);
    }

    String getXmlPath() {
        return new File(tempDir, imageName + ".xml").getAbsolutePath();
    }

    String getZarrPath() {
        return new File(tempDir, imageName + ".ome.zarr").getAbsolutePath();
    }


    String writeImageAndGetPath( ImagePlus imp, ImageDataFormat imageDataFormat,
                                 int[][] resolutions, int[][] subdivisions ) {
        String filePath;

        // gzip compression by default
        switch( imageDataFormat ) {
            case BdvN5:
                filePath = getXmlPath();
                new WriteImgPlusToN5().export( imp, resolutions, subdivisions, filePath,
                    sourceTransform, downsamplingMethod, compression, new String[]{imageName} );
                break;

            case BdvOmeZarr:
                filePath = getXmlPath();
                new WriteImgPlusToN5BdvOmeZarr().export( imp, resolutions, subdivisions, filePath,
                        sourceTransform, downsamplingMethod, compression, new String[]{imageName} );
                break;

            case OmeZarr:
                filePath = getZarrPath();
                new WriteImgPlusToN5OmeZarr().export( imp, resolutions, subdivisions, filePath,
                        sourceTransform, downsamplingMethod, compression, new String[]{imageName} );
                break;

            default:
                throw new UnsupportedOperationException();

        }

        return filePath;
    }

    String writeImageAndGetPath( ImageDataFormat imageDataFormat ) {

        // make an image with random values, same size as the imagej sample head image
        ImagePlus imp = makeImage( imageName, 186, 226, 27 );

        String filePath;

        // gzip compression by default
        switch( imageDataFormat ) {
            case BdvN5:
                filePath = getXmlPath();
                new WriteImgPlusToN5().export(imp, filePath, sourceTransform, downsamplingMethod,
                        compression, new String[]{imageName} );
                break;

            case BdvOmeZarr:
                filePath = getXmlPath();
                new WriteImgPlusToN5BdvOmeZarr().export(imp, filePath, sourceTransform,
                        downsamplingMethod, compression, new String[]{imageName} );
                break;

            case OmeZarr:
                filePath = getZarrPath();
                new WriteImgPlusToN5OmeZarr().export(imp, filePath, sourceTransform,
                        downsamplingMethod, compression, new String[]{imageName});
                break;

            default:
                throw new UnsupportedOperationException();

        }

        return filePath;
    }

    VolatileCachedCellImg getImage(SpimData spimData, int setupId, int timepoint, int level ) {
        MultiResolutionSetupImgLoader<?> imageLoader = (MultiResolutionSetupImgLoader<?>) spimData.
                getSequenceDescription().getImgLoader().getSetupImgLoader( setupId );

        return (VolatileCachedCellImg) imageLoader.getImage( timepoint, level );
    }

     boolean  isImageIdentical( VolatileCachedCellImg image1, VolatileCachedCellImg image2 ) {

        boolean isIdentical = true;

        CellLocalizingCursor cursorInput = image1.localizingCursor();
        RandomAccess randomAccessImage2 = image2.randomAccess();

        while ( cursorInput.hasNext()) {
            cursorInput.fwd();

            GenericByteType image1Value = (GenericByteType) cursorInput.get();
            GenericByteType image2Value = (GenericByteType) randomAccessImage2.setPositionAndGet( cursorInput );

            if ( !image1Value.equals(image2Value) ) {
                isIdentical = false;
                break;
            }
        }
        return isIdentical;
    }

    @Test
    void writeAndReadImageBdvN5() throws SpimDataException {
        ImageDataFormat format = ImageDataFormat.BdvN5;
        String xmlPath = writeImageAndGetPath( format );
        assertTrue( new File(xmlPath).exists() );
        assertTrue( new File(removeExtension(xmlPath) + ".n5").exists() );

        new SpimDataOpener().openSpimData( xmlPath, format );
    }

    @Test
    void writeAndReadImageOmeZarr() throws SpimDataException {
        ImageDataFormat format = ImageDataFormat.OmeZarr;
        String zarrPath = writeImageAndGetPath( ImageDataFormat.OmeZarr );
        assertTrue( new File(zarrPath).exists() );

        new SpimDataOpener().openSpimData( zarrPath, format );
    }

    @Test
    void writeAndReadImageBdvOmeZarr() throws SpimDataException {
        ImageDataFormat format = ImageDataFormat.BdvOmeZarr;
        String xmlPath = writeImageAndGetPath( ImageDataFormat.BdvOmeZarr );
        assertTrue( new File(xmlPath).exists() );
        assertTrue( new File(removeExtension(xmlPath) + ".ome.zarr").exists() );

        new SpimDataOpener().openSpimData( xmlPath, format );
    }

    @Test
    void checkOmeZarrEdges() throws SpimDataException {
        // check that edges in most downsampled levels are written properly for ome-zarr (i.e. that the loopback
        // is working correctly)
        // related to https://github.com/mobie/mobie-viewer-fiji/issues/572

        // use resolutions / subdivisions that trigger 'loopback' i.e. reading from previously downsampled levels
        // rather than the original image
        int[][] resolutions = new int[][]{ {1, 1, 1}, {2, 2, 2}, {4, 4, 4} };
        int[][] subdivisions = new int[][]{ {64, 64, 64}, {64, 64, 64}, {64, 64, 64} };
        int lowestResolutionLevel = 2;
        ImagePlus imp = makeImage( imageName, 400, 400, 400);

        String zarrPath = writeImageAndGetPath( imp, ImageDataFormat.OmeZarr, resolutions, subdivisions );
        String n5Path = writeImageAndGetPath(imp, ImageDataFormat.BdvN5, resolutions, subdivisions );

        SpimDataOpener spimDataOpener = new SpimDataOpener();
        SpimData spimDataZarr = (SpimData) spimDataOpener.openSpimData( zarrPath, ImageDataFormat.OmeZarr );
        SpimData spimDataN5 = (SpimData) spimDataOpener.openSpimData( n5Path, ImageDataFormat.BdvN5 );

        VolatileCachedCellImg lowestResN5 = getImage( spimDataN5, 0, 0, lowestResolutionLevel );
        VolatileCachedCellImg lowestResZarr = getImage( spimDataZarr, 0, 0, lowestResolutionLevel );

        // check lowest resolution level of n5 and ome-zarr have identical pixel values
        assertTrue( isImageIdentical( lowestResN5, lowestResZarr ) );
    }
}
