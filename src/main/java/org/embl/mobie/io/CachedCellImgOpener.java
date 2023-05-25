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

		final String dataset = "exported_data";

		ArrayList< String > axes = getIlastikAxesLabels( n5, dataset );

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

	private ArrayList< String > getIlastikAxesLabels( N5HDF5Reader n5, String dataset ) throws IOException
	{
		ArrayList< String > axes = new ArrayList<>();
		final JsonObject axistags = n5.getAttribute( dataset,"axistags", JsonObject.class );
		final JsonArray jsonArray = axistags.get( "axes" ).getAsJsonArray();
		for ( JsonElement jsonElement : jsonArray )
		{
			final JsonObject jsonObject = jsonElement.getAsJsonObject();
			final String axisLabel = jsonObject.get( "key" ).getAsString();
			axes.add( axisLabel );
		}
		Collections.reverse( axes );
		return axes;
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