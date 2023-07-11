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
package org.embl.mobie.io;

import bdv.cache.SharedQueue;
import bdv.util.volatiles.VolatileTypeMatcher;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import org.janelia.saalfeldlab.n5.hdf5.N5HDF5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CachedCellImgOpener< T extends NativeType< T > & RealType< T > >
{
	private final String path;
	private final ImageDataFormat imageDataFormat;
	private final SharedQueue sharedQueue;
	private boolean isOpen = false;
	private List< RandomAccessibleInterval< T > > channels;
	private ArrayList< RandomAccessibleInterval< Volatile< T > > > volatileChannels;
	private T type;

	public CachedCellImgOpener( String path, ImageDataFormat imageDataFormat, SharedQueue sharedQueue )
	{
		this.path = path;
		this.imageDataFormat = imageDataFormat;
		this.sharedQueue = sharedQueue;
	}

	private void open()
	{
		if ( isOpen )
			return;

		try
		{
			switch ( imageDataFormat )
			{
				case IlastikHDF5:
					openIlastikHDF5();
					break;
				default:
					throw new UnsupportedOperationException( "Cannot open " + imageDataFormat );
			}
		} catch ( Exception e )
		{
			e.printStackTrace();
			throw new RuntimeException( e );
		}
	}

	private void openIlastikHDF5() throws IOException
	{
		final N5HDF5Reader n5 = new N5HDF5Reader( path );

		final String dataset = "exported_data";
		//final JsonArray jsonArray = n5.getAttributes( dataset ).get( "DIMENSION_LABELS" ).getAsJsonArray();
		// TODO: get from JSON
		final String[] axesArray = { "c", "x", "y", "t" };
		final List< String > axes = Arrays.asList( axesArray );

//		this.axes = new Axes( axes );

		final CachedCellImg< T, ? > cachedCellImg = N5Utils.openVolatile( n5, dataset );
		channels = Axes.getChannels( cachedCellImg, axes );
		type = Util.getTypeFromInterval( channels.get( 0 ) );
		final NativeType< ? > volatileTypeForType = VolatileTypeMatcher.getVolatileTypeForType( type );

		RandomAccessibleInterval< Volatile< T > > vRAI;
		if ( sharedQueue == null )
		{
			vRAI = VolatileViews.wrapAsVolatile( cachedCellImg );
		}
		else
		{
			vRAI = VolatileViews.wrapAsVolatile( cachedCellImg, sharedQueue );
		}
		volatileChannels = Axes.getChannels( vRAI, axes );


		isOpen = true;
	}

	public RandomAccessibleInterval< T > getRAI( int c )
	{
		open();
		return channels.get( c );
	}

	public RandomAccessibleInterval< Volatile< T > > getVolatileRAI( int c )
	{
		open();
		return volatileChannels.get( c );
	}

	public int getNumChannels()
	{
		open();
		return channels.size();
	}

	public T getType()
	{
		return Util.getTypeFromInterval( channels.get( 0  ) );
	}
}
