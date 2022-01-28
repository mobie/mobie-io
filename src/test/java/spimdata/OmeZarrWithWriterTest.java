package spimdata;

import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.realtransform.AffineTransform3D;
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

    @BeforeEach
    void setUp( @TempDir Path tempDir ) throws IOException {
        this.tempDir = tempDir.toFile();
        imageName = "testImage";
        sourceTransform = new AffineTransform3D();
    }

    public static ImagePlus makeImage( String imageName ) {
        // make an image with random values, same size as the imagej sample head image
        return IJ.createImage(imageName, "8-bit noise", 186, 226, 27);
    }

    String writeImageAndGetPath( ImageDataFormat imageDataFormat ) {
        ImagePlus imp = makeImage( imageName );
        DownsampleBlock.DownsamplingMethod downsamplingMethod = DownsampleBlock.DownsamplingMethod.Average;
        Compression compression = new GzipCompression();
        String filePath;

        // gzip compression by default
        switch( imageDataFormat ) {
            case BdvN5:
                filePath = new File(tempDir, imageName + ".xml").getAbsolutePath();
                new WriteImgPlusToN5().export(imp, filePath, sourceTransform, downsamplingMethod,
                        compression, new String[]{imageName} );
                break;

            case BdvOmeZarr:
                filePath = new File(tempDir, imageName + ".xml").getAbsolutePath();
                new WriteImgPlusToN5BdvOmeZarr().export(imp, filePath, sourceTransform,
                        downsamplingMethod, compression, new String[]{imageName} );
                break;

            case OmeZarr:
                filePath = new File(tempDir, imageName + ".ome.zarr").getAbsolutePath();
                new WriteImgPlusToN5OmeZarr().export(imp, filePath, sourceTransform,
                        downsamplingMethod, compression, new String[]{imageName});
                break;

            default:
                throw new UnsupportedOperationException();

        }

        return filePath;
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
}
