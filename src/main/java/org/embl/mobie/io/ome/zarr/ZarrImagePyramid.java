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

import bdv.img.cache.VolatileCachedCellImg;
import bdv.util.AxisOrder;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileTypeMatcher;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import org.embl.mobie.io.ome.zarr.util.OmeZarrMultiscales;
import org.embl.mobie.io.ome.zarr.util.ZarrAxes;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrReader;

import javax.annotation.Nullable;

import static org.embl.mobie.io.ome.zarr.util.OmeZarrMultiscales.MULTI_SCALE_KEY;

class ZarrImagePyramid< T extends NativeType< T > & RealType< T >, V extends Volatile< T > & NativeType< V > & RealType< V > > implements ImagePyramid<T,V>
{
	private int numResolutions;

	private int numChannels;

	private int numTimepoints;

	private AxisOrder axisOrder;

	private T type;

	private V volatileType;

	private final SharedQueue queue;

	private final boolean writable;

	private CachedCellImg< T, ? >[] imgs;

	private RandomAccessibleInterval< V > [] vimgs;

	private final String imagePyramidPath;

	private ZarrAxes zarrAxes;

	/**
	 * TODO
	 */
	public ZarrImagePyramid(
			final String imagePyramidPath,
			@Nullable final SharedQueue queue,
			final boolean writable // TODO ??
	) throws Error
	{
		this.imagePyramidPath = imagePyramidPath;
		this.queue = queue;
		this.writable = writable; // TODO ??
	}

	// TODO: Getter
	//  - resolutions
	//  - either images or paths

	// TODO: Getter for axis metadata

	private void init()
	{
		if ( imgs != null ) return;

		try
		{
			final N5ZarrReader n5ZarrReader = new N5ZarrReader( imagePyramidPath );

			// Fetch multiscales metadata
			//
			OmeZarrMultiscales[] multiscales = n5ZarrReader.getAttribute( imagePyramidPath, MULTI_SCALE_KEY, OmeZarrMultiscales[].class );
			numResolutions = multiscales.length;

			// Set axes metadata.
			//
			// Fetch this metadata just from the highest
			// resolution level, assuming this is the
			// same for all resolutions
			// TODO is this assumption valid?
			final OmeZarrMultiscales multiscale = multiscales[ 0 ];
			zarrAxes = multiscale.axes;
			DatasetAttributes attributes = n5ZarrReader.getDatasetAttributes( multiscale.datasets[ 0 ].path );
			numChannels = zarrAxes.hasChannels() ? ( int ) attributes.getDimensions()[ zarrAxes.channelIndex() ] : 1;
			numTimepoints = zarrAxes.hasTimepoints() ? ( int ) attributes.getDimensions()[ zarrAxes.timeIndex() ] : 1;

			// Initialize the images
			//
			imgs = new CachedCellImg[ numResolutions ];
			vimgs = new VolatileCachedCellImg[ numResolutions ];

			for ( int resolution = 0; resolution < numResolutions; ++resolution )
			{
				// TODO handle S3
				imgs[ resolution ] = N5Utils.openVolatile( n5ZarrReader, imagePyramidPath );

				if ( queue != null )
					vimgs[ resolution ] = VolatileViews.wrapAsVolatile( imgs[ resolution ], queue );
				else
					vimgs[ resolution ] = VolatileViews.wrapAsVolatile( imgs[ resolution ] );
			}
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	private void initTypes()
	{
		if ( type != null ) return;

		init();

		// TODO can we get the type without accessing the data?
		type = Util.getTypeFromInterval( imgs[ 0 ] );
		volatileType = ( V ) VolatileTypeMatcher.getVolatileTypeForType( type );
	}

	@Override
	public int numResolutions()
	{
		init();
		return numResolutions;
	}

	@Override
	public AxisOrder axisOrder()
	{
		init();
		return axisOrder;
	}

	@Override
	public int numChannels()
	{
		init();
		return numChannels;
	}

	@Override
	public int numTimepoints()
	{
		init();
		return numTimepoints;
	}

	@Override
	public CachedCellImg< T, ? > getImg( final int resolutionLevel )
	{
		init();
		return imgs[ resolutionLevel ];
	}

	@Override
	public RandomAccessibleInterval< V > getVolatileImg( final int resolutionLevel )
	{
		init();
		return vimgs[ resolutionLevel ];
	}

	@Override
	public T getType()
	{
		initTypes();
		return type;
	}

	@Override
	public V getVolatileType()
	{
		initTypes();
		return volatileType;
	}

	public SharedQueue getSharedQueue()
	{
		return queue;
	}

}
