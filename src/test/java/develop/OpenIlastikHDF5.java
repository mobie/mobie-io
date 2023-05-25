package develop;

import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.util.RandomAccessibleIntervalSource4D;
import bdv.viewer.Source;
import com.google.gson.JsonElement;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.embl.mobie.io.CachedCellImgOpener;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.hdf5.N5HDF5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.metadata.axisTransforms.TransformAxes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class OpenIlastikHDF5
{
	public static void main( String[] args ) throws IOException
	{
		final CachedCellImgOpener< ? > opener = new CachedCellImgOpener( "/Users/tischer/Desktop/mobie-data/ilastik/A1_2022-07-06-093303-0000--0.4.0-0-1.4.0--tracking-oids.h5", ImageDataFormat.IlastikHDF5, null );
		final RandomAccessibleInterval< ? > rai = opener.getRAI( 0 );
		final RandomAccessibleInterval< ? > vRAI = opener.getVolatileRAI( 0 );
		int a = 1;
	}
}
