/*-
 * #%L
 * Expose the Imaris XT interface as an ImageJ2 service backed by ImgLib2.
 * %%
 * Copyright (C) 2019 - 2021 Bitplane AG
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
package org.embl.mobie.io.ome.zarr;

import Imaris.IDataSetPrx;
import bdv.img.cache.CreateInvalidVolatileCell;
import bdv.img.cache.VolatileCachedCellImg;
import bdv.util.AxisOrder;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileTypeMatcher;
import bdv.util.volatiles.VolatileViews;
import com.bitplane.xt.util.ImarisDirtyLoaderRemover;
import com.bitplane.xt.util.ImarisLoader;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.Cache;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.IoSync;
import net.imglib2.cache.LoaderCache;
import net.imglib2.cache.LoaderRemoverCache;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.EmptyCellCacheLoader;
import net.imglib2.cache.ref.GuardedStrongRefLoaderRemoverCache;
import net.imglib2.cache.ref.SoftRefLoaderCache;
import net.imglib2.cache.ref.SoftRefLoaderRemoverCache;
import net.imglib2.cache.ref.WeakRefVolatileCache;
import net.imglib2.cache.util.KeyBimap;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.VolatileCache;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.ArrayDataAccessFactory;
import net.imglib2.img.basictypeaccess.volatiles.VolatileArrayDataAccess;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.NativeTypeFactory;
import net.imglib2.type.numeric.RealType;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.imglib2.cache.volatiles.LoadingStrategy.BUDGETED;
import static net.imglib2.img.basictypeaccess.AccessFlags.DIRTY;
import static net.imglib2.img.basictypeaccess.AccessFlags.VOLATILE;

class ZarrImagePyramid< T extends NativeType< T > & RealType< T >, V extends Volatile< T > & NativeType< V > & RealType< V > > implements ImagePyramid< T, V >
{
	/**
	 * Number of resolutions pyramid levels
	 */
	private final int numResolutions;

	private final AxisOrder axisOrder;

	/**
	 * Dimensions of the resolution pyramid images.
	 * {@code dimensions[level][d]}.
	 */
	// TODO local variable
	private final long[][] dimensions;

	/**
	 * Cell sizes of the resolution pyramid images.
	 * {@code cellDimensions[level][d]}.
	 * C, and T cell size are always {@code = 1}.
	 */
	// TODO local variable
	private final int[][] cellDimensions;

	private final T type;

	private final V volatileType;

	private final SharedQueue queue;

	private RandomAccessibleInterval< T >[] imgs;

	private RandomAccessibleInterval< V >[] vimgs;

	private final int numChannels;

	private final int numTimepoints;

	private final String zArrayPath;

	/**
	 * TODO
	 */
	public ZarrImagePyramid(
			final T type,
			final String zArrayPath,
			final long[][] dimensions,
			final int[] mapDimensions,
			final SharedQueue queue,
			final boolean writable,
			final boolean isEmptyDataset ) throws Error
	{
		this.zArrayPath = zArrayPath;

		this.axisOrder = axisOrder;
		numResolutions = dimensions.length;
		this.dimensions = dimensions;
		this.cellDimensions = cellDimensions;

		numChannels = axisOrder.hasChannels() ? ( int ) dimensions[ 0 ][ axisOrder.channelDimension() ] : 1;
		numTimepoints = axisOrder.hasTimepoints() ? ( int ) dimensions[ 0 ][ axisOrder.timeDimension() ] : 1;

		this.type = type;
		volatileType = ( V ) VolatileTypeMatcher.getVolatileTypeForType( type );

		this.queue = queue;
	}

	private void initImgs() throws IOException
	{
		if ( imgs != null ) return;

		// TODO fetch numResolutions
		//   and other metadata from zArrayPath

		imgs = new CachedCellImg[ numResolutions ];
		vimgs = new VolatileCachedCellImg[ numResolutions ];

		for ( int resolution = 0; resolution < numResolutions; ++resolution )
		{
			// TODO handle S3
			final N5ZarrReader reader = new N5ZarrReader( zArrayPath );
			imgs[ resolution ] = N5Utils.open( reader, zArrayPath );
			vimgs[ resolution ] = VolatileViews.wrapAsVolatile( imgs[ resolution ] );
		}

	}

	/**
	 * Key for a cell identified by resolution level and index
	 * (flattened spatial coordinate).
	 */
	static class Key
	{
		private final int level;

		private final long index;

		private final int hashcode;

		/**
		 * Create a Key for the specified cell. Note that {@code cellDims} and
		 * {@code cellMin} are not used for {@code hashcode()/equals()}.
		 *
		 * @param level
		 *            level coordinate of the cell
		 * @param index
		 *            index of the cell (flattened spatial coordinate of the
		 *            cell)
		 */
		public Key( final int level, final long index )
		{
			this.level = level;
			this.index = index;
			hashcode = 31 * Long.hashCode( index ) + level;
		}

		@Override
		public boolean equals( final Object other )
		{
			if ( this == other )
				return true;
			if ( !( other instanceof Key ) )
				return false;
			final Key that = ( Key ) other;
			return ( this.index == that.index ) && ( this.level == that.level );
		}

		@Override
		public int hashCode()
		{
			return hashcode;
		}
	}

	@Override
	public int numResolutions()
	{
		return numResolutions;
	}

	@Override
	public AxisOrder axisOrder()
	{
		return axisOrder;
	}

	@Override
	public int numChannels()
	{
		return numChannels;
	}

	@Override
	public int numTimepoints()
	{
		return numTimepoints;
	}

	@Override
	public RandomAccessibleInterval< T > getImg( final int resolutionLevel )
	{
		initImgs();
		return imgs[ resolutionLevel ];
	}



	@Override
	public VolatileCachedCellImg< V, A > getVolatileImg( final int resolutionLevel )
	{
		return vimgs[ resolutionLevel ];
	}

	@Override
	public T getType()
	{
		return type;
	}

	@Override
	public V getVolatileType()
	{
		return volatileType;
	}

	/**
	 * Persist changes back to Imaris.
	 * Note that only the full resolution (level 0) image is writable!
	 */
	public void persist()
	{
		imgs[ 0 ].getCache().persistAll();
	}

	/**
	 * Invalidate cache for all levels of the resolution pyramid, except the full resolution.
	 * This is necessary when modifying a dataset and at the same time visualizing it in BigDataViewer.
	 * (This scenario is not very likely in practice, but still...)
	 * While actual modifications to the full-resolution image are immediately visible, updating the resolution pyramid needs to go through Imaris.
	 */
	public void invalidate() // TODO: rename!?
	{
		// TODO: from level 0 or 1?
		//       or should we have both?
		for ( int i = 1; i < vimgs.length; i++ )
			vimgs[ i ].getCache().invalidateAll();
	}

	public SharedQueue getSharedQueue()
	{
		return queue;
	}

	/**
	 * Split this {@code ImagePyramid} along the channel axis (according to the {@code axisOrder}.
	 * Returns a list of {@code ImagePyramid}s, one for each channel.
	 * If this {@code ImagePyramid} has no Z dimension, it is augmented by a Z dimension of size 1.
	 * Thus, the returned {@code ImagePyramid}s are always 3D (XYZ) or 4D (XYZT).
	 */
	public List< ImagePyramid< T, V > > splitIntoSourceStacks()
	{
		return splitIntoSourceStacks( this );
	}

	/**
	 * Takes an {@code ImagePyramid} and splits it along the channel axis (according to the pyramid's {@code axisOrder}.
	 * Returns a list of {@code ImagePyramid}s, one for each channel.
	 * If the input {@code ImagePyramid} has no Z dimension, it is augmented by a Z dimension of size 1.
	 * Thus, the returned {@code ImagePyramid}s are always 3D (XYZ) or 4D (XYZT).
	 */
	private static < T, V > List< ImagePyramid< T, V > > splitIntoSourceStacks( final ImagePyramid< T, V > input )
	{
		final int numResolutions = input.numResolutions();
		final int numChannels = input.numChannels();

		final List< DefaultImagePyramid< T, V > > channels = new ArrayList<>( numChannels );
		final AxisOrder axisOrder = splitIntoSourceStacks( input.axisOrder() );
		for ( int c = 0; c < numChannels; c++ )
			channels.add( new DefaultImagePyramid<>( input.getType(), input.getVolatileType(), numResolutions, axisOrder ) );

		for ( int l = 0; l < numResolutions; ++l )
		{
			final List< RandomAccessibleInterval< T > > channelImgs = AxisOrder.splitInputStackIntoSourceStacks( input.getImg( l ), input.axisOrder() );
			final List< RandomAccessibleInterval< V > > channelVolatileImgs = AxisOrder.splitInputStackIntoSourceStacks( input.getVolatileImg( l ), input.axisOrder() );
			for ( int c = 0; c < numChannels; ++c )
			{
				channels.get( c ).imgs[ l ] = channelImgs.get( c );
				channels.get( c ).vimgs[ l ] = channelVolatileImgs.get( c );
			}
		}

		return new ArrayList<>( channels );
	}

	/**
	 * Returns the {@code AxisOrder} of (each channel) of a {@code ImagePyramid} split into source stacks.
	 * Basically: remove the channel dimension. If there is no Z dimension, add one.
	 */
	private static AxisOrder splitIntoSourceStacks( final AxisOrder axisOrder )
	{
		switch ( axisOrder )
		{
		case XYZ:
		case XYZC:
		case XY:
		case XYC:
		case XYCZ:
			return AxisOrder.XYZ;
		case XYZT:
		case XYZCT:
		case XYZTC:
		case XYCZT:
		case XYT:
		case XYCT:
		case XYTC:
			return AxisOrder.XYZT;
		case DEFAULT:
		default:
			throw new IllegalArgumentException();
		}
	}
}
