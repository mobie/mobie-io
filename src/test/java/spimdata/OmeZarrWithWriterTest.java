package spimdata;

import bdv.img.cache.VolatileCachedCellImg;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import mpicbg.spim.data.sequence.SequenceDescription;
import mpicbg.spim.data.sequence.ViewSetup;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccess;
import net.imglib2.img.cell.CellLocalizingCursor;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.GenericByteType;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;
import org.embl.mobie.io.n5.util.DownsampleBlock;
import org.embl.mobie.io.n5.writers.WriteImagePlusToN5;
import org.embl.mobie.io.ome.zarr.util.ZarrAxes;
import org.embl.mobie.io.ome.zarr.writers.imageplus.WriteImagePlusToN5OmeZarr;
import org.embl.mobie.io.util.IOHelper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.GzipCompression;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OmeZarrWithWriterTest {

    private String imageName;
    private AffineTransform3D sourceTransform;
    private File tempDir;
    private DownsampleBlock.DownsamplingMethod downsamplingMethod;
    private Compression compression;
    private static JSONObject omeZarrSchema;

    private int defaultWidth;
    private int defaultHeight;
    private int defaultDepth;
    private int defaultNChannels;
    private int defaultNTimepoints;

    @BeforeAll
    static void downloadSchema() throws IOException {
        try( InputStream schemaInputStream = IOHelper.getInputStream(
                "https://raw.githubusercontent.com/ome/ngff/main/0.4/schemas/image.schema") ) {
            omeZarrSchema = new JSONObject(new JSONTokener(schemaInputStream));
        }
    }

    @BeforeEach
    void setUp( @TempDir Path tempDir ) throws IOException {
        this.tempDir = tempDir.toFile();
        imageName = "testImage";
        sourceTransform = new AffineTransform3D();
        downsamplingMethod = DownsampleBlock.DownsamplingMethod.Average;
        compression = new GzipCompression();
        // same dimensions as ImageJ sample head image
        defaultWidth = 186;
        defaultHeight = 226;
        defaultDepth = 27;
        defaultNChannels = 2;
        defaultNTimepoints = 2;
    }

    ImagePlus makeZYXImage( String imageName, int width, int height, int depth ) {
        // make an image with random values
        return IJ.createImage(imageName, "8-bit noise", width, height, depth);
    }

    ImagePlus makeCZYXImage( String imageName, int width, int height, int depth, int channels ) {
        return IJ.createImage(imageName, "8-bit ramp", width, height, channels, depth, 1 );
    }
    ImagePlus makeTZYXImage( String imageName, int width, int height, int depth, int timePoints ) {
        return IJ.createImage(imageName, "8-bit ramp", width, height, 1, depth, timePoints );
    }

    ImagePlus makeTCZYXImage( String imageName, int width, int height, int depth, int channels, int timePoints ) {
        return IJ.createImage(imageName, "8-bit ramp", width, height, channels, depth, timePoints );
    }

    String getXmlPath() {
        return new File(tempDir, imageName + ".xml").getAbsolutePath();
    }

    String getZarrPath() {
        return new File(tempDir, imageName + ".ome.zarr").getAbsolutePath();
    }

    String[] getViewSetupNames( ImagePlus imp, ImageDataFormat imageDataFormat ) {
        int nChannels = imp.getNChannels();
        return getViewSetupNames( nChannels, imageDataFormat );
    }

    String[] getViewSetupNames( int nChannels, ImageDataFormat imageDataFormat ) {
        String[] viewSetupNames = new String[nChannels];

        if ( nChannels == 1 ) {
            viewSetupNames[0] = imageName;
        } else {
            if ( imageDataFormat == ImageDataFormat.BdvN5 ) {
                // N5 supports different names for each setup - so we name by channel
                for ( int i = 0; i < nChannels; i++ ) {
                    viewSetupNames[i] = imageName + "-channel" + (i + 1);
                }
            } else {
                // Ome-Zarr uses the image name for every setup (doesn't support different names currently)
                for ( int i = 0; i < nChannels; i++ ) {
                    viewSetupNames[i] = imageName;
                }
            }
        }

        return viewSetupNames;
    }

    void validateJSON( String zarrPath ) throws IOException {
        String zattrsPath = new File(zarrPath, ".zattrs").getAbsolutePath();

        try( InputStream zattrsInputStream = new FileInputStream( zattrsPath ) ) {
            JSONObject jsonSubject = new JSONObject(new JSONTokener(zattrsInputStream));

            // library only supports up to draft 7 json schema - specify here, otherwise errors when reads 2020-12 in
            // the schema file
            SchemaLoader loader = SchemaLoader.builder()
                    .schemaJson(omeZarrSchema)
                    .draftV7Support()
                    .build();
            Schema schema = loader.load().build();

            schema.validate(jsonSubject);
        }
    }

    void spimDataAssertions( SpimData spimData, int nChannels, int nTimepoints, ImageDataFormat imageDataFormat ) {
        SequenceDescription sequenceDescription = spimData.getSequenceDescription();
        assertEquals( sequenceDescription.getTimePoints().size(), nTimepoints );
        assertEquals( sequenceDescription.getViewSetupsOrdered().size(), nChannels );

        // check view setup names
        String[] viewSetupNames = getViewSetupNames(nChannels, imageDataFormat);
        List<ViewSetup> viewSetups = sequenceDescription.getViewSetupsOrdered();
        for ( int i = 0; i<viewSetups.size(); i++ ) {
            assertEquals( viewSetups.get(i).getName(), viewSetupNames[i] );
        }

        Dimensions dimensions = sequenceDescription.getViewSetupsOrdered().get(0).getSize();
        assertEquals( dimensions.dimension(0), defaultWidth );
        assertEquals( dimensions.dimension(1), defaultHeight );
        assertEquals( dimensions.dimension(2), defaultDepth );
    }

    void n5Assertions( String xmlPath, int nChannels, int nTimepoints ) throws SpimDataException {
        assertTrue( new File(xmlPath).exists() );
        assertTrue( new File(removeExtension(xmlPath) + ".n5").exists() );

        SpimData spimData = (SpimData) new SpimDataOpener().openSpimData( xmlPath, ImageDataFormat.BdvN5 );
        spimDataAssertions( spimData, nChannels, nTimepoints, ImageDataFormat.BdvN5 );
    }

    void zarrAssertions( String zarrPath, int nChannels, int nTimepoints ) throws SpimDataException, IOException {
        assertTrue( new File(zarrPath).exists() );
        validateJSON( zarrPath );

        SpimData spimData = (SpimData) new SpimDataOpener().openSpimData( zarrPath, ImageDataFormat.OmeZarr );
        spimDataAssertions( spimData, nChannels, nTimepoints, ImageDataFormat.OmeZarr );
    }

    String writeImageAndGetPath( ImagePlus imp, ImageDataFormat imageDataFormat,
                                 int[][] resolutions, int[][] subdivisions ) {
        String filePath;

        // gzip compression by default
        switch( imageDataFormat ) {
            case BdvN5:
                filePath = getXmlPath();
                new WriteImagePlusToN5().export( imp, resolutions, subdivisions, filePath,
                    sourceTransform, downsamplingMethod, compression, getViewSetupNames(imp, imageDataFormat) );
                break;

            case OmeZarr:
                filePath = getZarrPath();
                new WriteImagePlusToN5OmeZarr().export( imp, resolutions, subdivisions, filePath,
                        sourceTransform, downsamplingMethod, compression, getViewSetupNames(imp, imageDataFormat) );
                break;

            default:
                throw new UnsupportedOperationException();

        }

        return filePath;
    }

    String writeImageAndGetPath( ImageDataFormat imageDataFormat, ZarrAxes axes ) {

        ImagePlus imp;
        if ( axes == ZarrAxes.ZYX ) {
            // make an image with random values, same size as the imagej sample head image
            imp = makeZYXImage(imageName, defaultWidth, defaultHeight, defaultDepth);
        } else if ( axes == ZarrAxes.CZYX ) {
            imp = makeCZYXImage(imageName, defaultWidth, defaultHeight, defaultDepth, defaultNChannels);
        } else if ( axes == ZarrAxes.TZYX ) {
            imp = makeTZYXImage(imageName, defaultWidth, defaultHeight, defaultDepth, defaultNTimepoints);
        } else if ( axes == ZarrAxes.TCZYX ){
            imp = makeTCZYXImage(imageName, defaultWidth, defaultHeight, defaultDepth,
                    defaultNChannels, defaultNTimepoints);
        } else {
            throw new UnsupportedOperationException("Unimplemented axis type");
        }

        String filePath;

        // gzip compression by default
        switch( imageDataFormat ) {
            case BdvN5:
                filePath = getXmlPath();
                new WriteImagePlusToN5().export(imp, filePath, sourceTransform, downsamplingMethod,
                        compression, getViewSetupNames(imp, imageDataFormat));
                break;

            case OmeZarr:
                filePath = getZarrPath();
                new WriteImagePlusToN5OmeZarr().export(imp, filePath, sourceTransform,
                        downsamplingMethod, compression, getViewSetupNames(imp, imageDataFormat));
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
    void writeAndReadZYXImageBdvN5() throws SpimDataException {
        ImageDataFormat format = ImageDataFormat.BdvN5;
        String xmlPath = writeImageAndGetPath( format, ZarrAxes.ZYX );

        n5Assertions( xmlPath, 1, 1 );
    }

    @Test
    void writeAndReadCZYXImageBdvN5() throws SpimDataException {
        ImageDataFormat format = ImageDataFormat.BdvN5;
        String xmlPath = writeImageAndGetPath( format, ZarrAxes.CZYX );

        n5Assertions( xmlPath, defaultNChannels, 1 );
    }

    @Test
    void writeAndReadTZYXImageBdvN5() throws SpimDataException {
        ImageDataFormat format = ImageDataFormat.BdvN5;
        String xmlPath = writeImageAndGetPath( format, ZarrAxes.TZYX );

        n5Assertions( xmlPath, 1, defaultNTimepoints );
    }

    @Test
    void writeAndReadTCZYXImageBdvN5() throws SpimDataException {
        ImageDataFormat format = ImageDataFormat.BdvN5;
        String xmlPath = writeImageAndGetPath( format, ZarrAxes.TCZYX );

        n5Assertions( xmlPath, defaultNChannels, defaultNTimepoints );
    }

    @Test
    void writeAndReadZYXImageOmeZarr() throws SpimDataException, IOException {
        ImageDataFormat format = ImageDataFormat.OmeZarr;
        String zarrPath = writeImageAndGetPath( format, ZarrAxes.ZYX );

        zarrAssertions( zarrPath, 1, 1 );
    }

    @Test
    void writeAndReadCZYXImageOmeZarr() throws SpimDataException, IOException {
        ImageDataFormat format = ImageDataFormat.OmeZarr;
        String zarrPath = writeImageAndGetPath( format, ZarrAxes.CZYX );

        zarrAssertions( zarrPath, defaultNChannels, 1 );
    }

    @Test
    void writeAndReadTZYXImageOmeZarr() throws SpimDataException, IOException {
        ImageDataFormat format = ImageDataFormat.OmeZarr;
        String zarrPath = writeImageAndGetPath( format, ZarrAxes.TZYX );

        zarrAssertions( zarrPath, 1, defaultNTimepoints );
    }

    @Test
    void writeAndReadTCZYXImageOmeZarr() throws SpimDataException, IOException {
        ImageDataFormat format = ImageDataFormat.OmeZarr;
        String zarrPath = writeImageAndGetPath( format, ZarrAxes.TCZYX );

        zarrAssertions( zarrPath, defaultNChannels, defaultNTimepoints );
    }

    @Test
    void checkOmeZarrLoopBack() throws SpimDataException {
        // check that most downsampled levels are written properly for ome-zarr (i.e. that the loopback
        // is working correctly)
        // related to https://github.com/mobie/mobie-viewer-fiji/issues/572

        // use resolutions / subdivisions that trigger 'loopback' i.e. reading from previously downsampled levels
        // rather than the original image
        int[][] resolutions = new int[][]{ {1, 1, 1}, {2, 2, 2}, {4, 4, 4} };
        int[][] subdivisions = new int[][]{ {64, 64, 64}, {64, 64, 64}, {64, 64, 64} };
        int lowestResolutionLevel = 2;
        ImagePlus imp = makeZYXImage( imageName, 400, 400, 400);

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
