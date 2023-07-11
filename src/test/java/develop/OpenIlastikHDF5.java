/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2023 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
		final CachedCellImgOpener< ? > opener = new CachedCellImgOpener( "/Users/tischer/Desktop/C5_2022-07-12-165037-0000--0.4.0-0-1.4.0--tracking-oids.h5", ImageDataFormat.IlastikHDF5, null );
		final RandomAccessibleInterval< ? > rai = opener.getRAI( 0 );
		final RandomAccessibleInterval< ? > vRAI = opener.getVolatileRAI( 0 );
		int a = 1;

//		final Class< String > aClass = String.class;
//		final boolean assignableFrom = aClass.isAssignableFrom( String[].class );
//		final N5HDF5Reader n5 = new N5HDF5Reader( "/Users/tischer/Desktop/C5_2022-07-12-165037-0000--0.4.0-0-1.4.0--tracking-oids.h5" );
//		final boolean isIlastikHDF5 = n5.datasetExists( "exported_data" );
//		final DatasetAttributes datasetAttributes = n5.getDatasetAttributes( "exported_data" );
//		final long[] dimensions = datasetAttributes.getDimensions();
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
