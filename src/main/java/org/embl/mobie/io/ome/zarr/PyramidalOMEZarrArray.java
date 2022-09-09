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
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileTypeMatcher;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import org.embl.mobie.io.ome.zarr.util.OmeZarrMultiscales;
import org.embl.mobie.io.ome.zarr.util.OMEZarrAxes;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrReader;

import javax.annotation.Nullable;

import static org.embl.mobie.io.ome.zarr.util.OmeZarrMultiscales.MULTI_SCALE_KEY;

class PyramidalOMEZarrArray< T extends NativeType< T > & RealType< T >, V extends Volatile< T > & NativeType< V > & RealType< V > > implements PyramidalArray< T, V >
{
	private int numResolutions;

	private T type;

	private V volatileType;

	private final SharedQueue queue;

	private CachedCellImg< T, ? >[] imgs;

	private RandomAccessibleInterval< V > [] vimgs;

	private final String imagePyramidPath;

	private OMEZarrAxes axes;

	private long[] dimensions;

	private OmeZarrMultiscales.CoordinateTransformations[] coordinateTransformations;

	private DatasetAttributes attributes;

	/**
	 * TODO
	 */
	public PyramidalOMEZarrArray(
			final String imagePyramidPath,
			@Nullable final SharedQueue queue
	) throws Error
	{
		this.imagePyramidPath = imagePyramidPath;
		this.queue = queue;
	}

	public OMEZarrAxes getAxes()
	{
		return axes;
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

			// Set metadata.
			//
			final OmeZarrMultiscales multiscale = multiscales[ 0 ];
			axes = multiscale.axes;
			attributes = n5ZarrReader.getDatasetAttributes( multiscale.datasets[ 0 ].path );
			initTypes( attributes );
			dimensions = attributes.getDimensions();
			coordinateTransformations = multiscale.coordinateTransformations;

			// Initialize images.
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

	private void initTypes( DatasetAttributes attributes )
	{
		if ( type != null ) return;

		// Note: we could also get that from the
		//   {@code imgs} but that may trigger loading
		//   some data, which may be time-consuming if it
		//   needs to fetch from the cloud.

		// TODO JOHN: Does the below code already exists
		//   somewhere in N5?
		final DataType dataType = attributes.getDataType();
		switch ( dataType ) {
			case UINT8:
				type = Cast.unchecked( new UnsignedByteType() );
				break;
			case UINT16:
				type = Cast.unchecked( new UnsignedShortType() );
				break;
			case UINT32:
				type = Cast.unchecked( new UnsignedIntType() );
				break;
			case UINT64:
				type = Cast.unchecked( new UnsignedLongType() );
				break;
			case INT8:
				type = Cast.unchecked( new ByteType() );
				break;
			case INT16:
				type = Cast.unchecked( new ShortType() );
				break;
			case INT32:
				type = Cast.unchecked( new IntType() );
				break;
			case INT64:
				type = Cast.unchecked( new LongType() );
				break;
			case FLOAT32:
				type = Cast.unchecked( new FloatType() );
				break;
			case FLOAT64:
				type = Cast.unchecked( new DoubleType() );
				break;
		}

		volatileType = ( V ) VolatileTypeMatcher.getVolatileTypeForType( type );
	}

	public OmeZarrMultiscales.CoordinateTransformations[] getCoordinateTransformations()
	{
		init();
		return coordinateTransformations;
	}

	@Override
	public long[] dimensions()
	{
		init();
		return dimensions;
	}

	@Override
	public int numResolutions()
	{
		init();
		return numResolutions;
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
		init();
		return type;
	}

	@Override
	public V getVolatileType()
	{
		init();
		return volatileType;
	}

	public SharedQueue getSharedQueue()
	{
		return queue;
	}

	@Override
	public int numDimensions()
	{
		return dimensions.length;
	}
}
