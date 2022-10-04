package playground;

import bdv.util.BdvFunctions;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;

public class OpenOMEZarr
{
	public static void main( String[] args ) throws SpimDataException
	{
		final AbstractSpimData spimData = new SpimDataOpener().openSpimData( "https://s3.embl.de/ome-zarr-course/ome_zarr_data/xyzct_8bit__mitosis.zarr", ImageDataFormat.OmeZarrS3 );
		BdvFunctions.show( spimData );
	}
}
