package spimdata;

import de.embl.cba.n5.util.writers.projectcreator.DownsampleBlock;
import de.embl.cba.n5.ome.zarr.writers.projectcreator.WriteImgPlusToN5BdvOmeZarr;
import de.embl.cba.n5.ome.zarr.writers.projectcreator.WriteImgPlusToN5OmeZarr;
import ij.IJ;
import ij.ImagePlus;
import org.janelia.saalfeldlab.n5.GzipCompression;

public class OmeZarrWithWriterTest {
    public static void main( String[] args ) {
        ImagePlus imp = IJ.openImage("src/test/resources/ImgForTest.tif");
        new WriteImgPlusToN5OmeZarr().export(imp, "src/test/resources/zyx.ome.zarr",
                DownsampleBlock.DownsamplingMethod.Average, new GzipCompression());

        new WriteImgPlusToN5BdvOmeZarr().export(imp, "src/test/resources/writingTest.xml",
                DownsampleBlock.DownsamplingMethod.Average, new GzipCompression());
    }
}
