import bdv.util.BdvFunctions;
import bdv.util.Prefs;
import mpicbg.spim.data.SpimData;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;

import java.io.IOException;

public class Deleteme
{
	public static void main( String[] args ) throws IOException
	{
		OMEZarrS3Opener.setLogging( true );
		SpimData spimData = OMEZarrS3Opener.readURL("https://s3.embl.de/i2k-2020/ngff-example-data/v0.4/zyx.ome.zarr");
		Prefs.showScaleBar(true);
		BdvFunctions.show( spimData );
		final String unit = spimData.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize().unit();
		final double[] voxelSize = spimData.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize().dimensionsAsDoubleArray();

	}
}
