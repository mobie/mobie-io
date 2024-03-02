package develop;

import ij.IJ;
import ij.ImagePlus;
import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5FSWriter;
import org.janelia.saalfeldlab.n5.ij.N5IJUtils;
import org.janelia.saalfeldlab.n5.ij.N5Importer;
import org.janelia.saalfeldlab.n5.ij.N5ScalePyramidExporter;

import java.io.IOException;

import static org.janelia.saalfeldlab.n5.ij.N5ScalePyramidExporter.GZIP_COMPRESSION;

public class WriteOMEZarr
{
    public static void main( String[] args ) throws IOException
    {
        ImagePlus imp = IJ.openImage( "http://imagej.net/images/mri-stack.zip" );

        N5ScalePyramidExporter exporter = new N5ScalePyramidExporter(
                imp,
                "/Users/tischer/Desktop/test/mri.ome.zarr",
                "/",
                "10,10,4",
                true,
                N5ScalePyramidExporter.DOWNSAMPLE_METHOD.Average,
                N5Importer.MetadataOmeZarrKey,
                GZIP_COMPRESSION
        );

        exporter.run();
    }
}
