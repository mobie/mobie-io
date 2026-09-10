package develop;

import ij.IJ;
import net.imagej.ImageJ;

public class DevelopShowMessage
{
    public static void main( String[] args )
    {
        new ImageJ().ui().showUI();
        IJ.showMessage(
				"OME-Zarr backend unavailable",
				"The selected OME-Zarr reader '" + "ABC" + "' is not available in this Fiji installation.\n" +
						"Missing class:" + "ABC" + "\n" +
                        " " + "\n" +
                        "To use this reader please enable the 'OME-Zarr' update site."
		);
    }
}
