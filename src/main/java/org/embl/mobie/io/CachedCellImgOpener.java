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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import org.janelia.saalfeldlab.n5.hdf5.N5HDF5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CachedCellImgOpener< T extends NativeType< T > & RealType< T > >
{
	private final String path;
	private final ImageDataFormat imageDataFormat;
	private final SharedQueue sharedQueue;
	private boolean isOpen = false;
	private List< RandomAccessibleInterval< T > > channelRAIs;
	private ArrayList< RandomAccessibleInterval< Volatile< T > > > volatileChannelRAIs;
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

		String dataset = "exported_data";
		if ( ! n5.datasetExists( dataset ) )
			dataset = "data";

		List< String > axes = fetchAxesLabels( n5, dataset );

		final CachedCellImg< T, ? > cachedCellImg = N5Utils.openVolatile( n5, dataset );
		channelRAIs = Axes.getChannels( cachedCellImg, axes );
		volatileChannelRAIs = Axes.getChannels( getVolatileRAI( cachedCellImg ), axes );
		type = Util.getTypeFromInterval( channelRAIs.get( 0 ) );
		isOpen = true;
	}

	private RandomAccessibleInterval< Volatile< T > > getVolatileRAI( CachedCellImg< T, ? > cachedCellImg )
	{
		if ( sharedQueue == null )
		{
			return VolatileViews.wrapAsVolatile( cachedCellImg );
		}
		else
		{
			return VolatileViews.wrapAsVolatile( cachedCellImg, sharedQueue );
		}
	}

	private static List< String > fetchAxesLabels( N5HDF5Reader n5, String dataset ) throws IOException
	{
		try
		{
			ArrayList< String > axes = new ArrayList<>();
			final JsonObject axisTags = n5.getAttribute( dataset, "axistags", JsonObject.class );
			final JsonArray jsonArray = axisTags.get( "axes" ).getAsJsonArray();
			for ( JsonElement jsonElement : jsonArray )
			{
				final JsonObject jsonObject = jsonElement.getAsJsonObject();
				final String axisLabel = jsonObject.get( "key" ).getAsString();
				axes.add( axisLabel );
			}
			Collections.reverse( axes );
			return axes;
		}
		catch ( Exception e )
		{
			List< String > axes = Arrays.asList( "t", "z", "y", "x", "c" );
			Collections.reverse( axes );
			return axes;
		}
	}

	public RandomAccessibleInterval< T > getRAI( int c )
	{
		open();
		return channelRAIs.get( c );
	}

	public RandomAccessibleInterval< Volatile< T > > getVolatileRAI( int c )
	{
		open();
		return volatileChannelRAIs.get( c );
	}

	public int getNumChannels()
	{
		open();
		return channelRAIs.size();
	}

	public T getType()
	{
		open();
		return type;
	}
}
