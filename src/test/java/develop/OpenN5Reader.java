package develop;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.universe.N5Factory;

public class OpenN5Reader
{
	public static void main( String[] args )
	{
		final N5Reader n5Reader = new N5Factory().openReader( "s3://your-bucket/myremote.zarr" );
	}
}
