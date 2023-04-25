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
//		final Class< String > aClass = String.class;
//		final boolean assignableFrom = aClass.isAssignableFrom( String[].class );
		final N5HDF5Reader n5 = new N5HDF5Reader( "/Users/tischer/Desktop/C5_2022-07-12-165037-0000--0.4.0-0-1.4.0--tracking-oids.h5" );
		final boolean isIlastikHDF5 = n5.datasetExists( "exported_data" );
		final DatasetAttributes datasetAttributes = n5.getDatasetAttributes( "exported_data" );
		final long[] dimensions = datasetAttributes.getDimensions();
		//final String[] attribute = n5.getAttribute( "exported_data", "DIMENSION_LABELS", String[].class );
		// TRY
		// Multiscales[] multiscalesArray = n5ZarrReader.getAttribute( "", MULTI_SCALE_KEY, Multiscales[].class );
		// final JsonArray multiscalesJsonArray = n5ZarrReader.getAttributes( "" ).get( MULTI_SCALE_KEY ).getAsJsonArray();
		//			for ( int i = 0; i < multiscalesArray.length; i++ )
		//			{
		//				multiscalesArray[ i ].applyVersionFixes( multiscalesJsonArray.get( i ).getAsJsonObject() );
		//				multiscalesArray[ i ].init();
		//			}

		/*
		imgs[ resolution ] = N5Utils.openVolatile( n5ZarrReader, datasets[ resolution ].path );

				if ( queue == null )
					vimgs[ resolution ] = VolatileViews.wrapAsVolatile( imgs[ resolution ] );
				else
					vimgs[ resolution ] = VolatileViews.wrapAsVolatile( imgs[ resolution ], queue );
		 */
//		final CachedCellImg< ?, ? > exportedData = N5Utils.openVolatile( n5, "exported_data" );
//
//		new TransformAxes(  )
//		new AxisOrder
//		BdvFunctions.show( Ra
//		final ArrayList< RandomAccessibleInterval< T > > stacks = AxisOrder.splitInputStackIntoSourceStacks( img, axisOrder );
//		int numTimepoints = 1;
//		for ( final RandomAccessibleInterval< T > stack : stacks )
//		{
//			final Source< T > s;
//			if ( stack.numDimensions() > 3 )
//			{
//				numTimepoints = ( int ) stack.max( 3 ) + 1;
//				s = new RandomAccessibleIntervalSource4D<>( stack, type, sourceTransform, name );
//			}
//			else
//			{
//				s = new RandomAccessibleIntervalSource<>( stack, type, sourceTransform, name );
//			}
//			addSourceToListsGenericType( s, handle.getUnusedSetupId(), converterSetups, sources );
//		}
		//new SpimDataOpener().open(  )
	}
}
