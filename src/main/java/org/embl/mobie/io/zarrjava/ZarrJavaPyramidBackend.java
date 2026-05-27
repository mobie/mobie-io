/*-
 * #%L
 * OME-Zarr extras for Fiji
 * %%
 * Copyright (C) 2022 - 2026 SciJava developers
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
package org.embl.mobie.io.zarrjava;

import bdv.BigDataViewer;
import bdv.cache.SharedQueue;
import bdv.util.RandomAccessibleIntervalMipmapSource4D;
import bdv.util.volatiles.VolatileTypeMatcher;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.SourceAndConverter;
import dev.zarr.zarrjava.ZarrException;
import dev.zarr.zarrjava.core.Array;
import dev.zarr.zarrjava.core.Attributes;
import dev.zarr.zarrjava.core.Group;
import dev.zarr.zarrjava.experimental.ome.MultiscaleImage;
import dev.zarr.zarrjava.experimental.ome.metadata.Axis;
import dev.zarr.zarrjava.experimental.ome.metadata.MultiscalesEntry;
import dev.zarr.zarrjava.experimental.ome.metadata.transform.CoordinateTransformation;
import dev.zarr.zarrjava.experimental.ome.metadata.transform.ScaleCoordinateTransformation;
import dev.zarr.zarrjava.store.FilesystemStore;
import dev.zarr.zarrjava.store.HttpStore;
import dev.zarr.zarrjava.store.Store;
import dev.zarr.zarrjava.store.StoreHandle;
import ij.IJ;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.converter.Converter;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;
import org.embl.mobie.io.exceptions.MultiImageDatasetException;
import org.embl.mobie.io.exceptions.NotAMultiscaleImageException;
import org.embl.mobie.io.exceptions.PyramidLevelAccessException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.embl.mobie.io.zarrjava.Omero.convertOmero;

// Copied and modified from https://github.com/BioImageTools/ome-zarr-fiji-java
public class ZarrJavaPyramidBackend<
		T extends NativeType< T > & RealType< T >,
		V extends Volatile< T > & NativeType< V > & RealType< V > >
{
	private final URI inputUri;

	private StoreHandle activeHandle = null;
	private T type;
	private V volatileType;
	private int numTimepoints;
	private int numChannels;
	private int numResolutionLevels;
	private CachedCellImg< T, ? >[] cachedCellImgs;
	private RandomAccessibleInterval< V >[] volatileImgs;
	private int channelAxisIndex;
	private int zAxisIndex;
	private int timeAxisIndex;
	private String[] channelLabels;
	private VoxelDimensions voxelDimensions;
	private AffineTransform3D[] transforms;

	public ZarrJavaPyramidBackend( final URI inputUri )
	{
		this.inputUri = inputUri;
	}

	public void load()
	{
		final MultiscaleImage multiscaleImage = openMultiscaleImage();
		final MultiscalesEntry entry = readMultiscalesEntry( multiscaleImage );

		numResolutionLevels = countResolutionLevels( multiscaleImage );

		final Array level0Array = openLevel( multiscaleImage, 0 );
		type = typeForZarrDataType( level0Array.metadata().dataType().getMA2DataType() );
		volatileType = Cast.unchecked( VolatileTypeMatcher.getVolatileTypeForType( type ) );

		// zarr shape is C-order [t, c, z, y, x]; imglib2 uses F-order [x, y, z, c, t]
		final long[] zarrShape = level0Array.metadata().shape;
		final long[] dimensions = reverseToLong( zarrShape );
		final int numDimensions = dimensions.length;

		numTimepoints = getDimSizeForAxis( entry.axes, zarrShape, AxisCalibration.T );
		numChannels = getDimSizeForAxis( entry.axes, zarrShape, AxisCalibration.C );

		final String name = entry.name != null ? entry.name : defaultName();
		final double[] level0Scales = getLevel0Scales( entry, numDimensions );

		final SharedQueue sharedQueue = new SharedQueue( Math.max( 1, Runtime.getRuntime().availableProcessors() / 2 ) );
		cachedCellImgs = Cast.unchecked( new CachedCellImg[ numResolutionLevels ] );
		volatileImgs = Cast.unchecked( new RandomAccessibleInterval[ numResolutionLevels ] );
		for ( int level = 0; level < numResolutionLevels; level++ )
		{
			final Array arr = openLevel( multiscaleImage, level );
			final long[] imgShape = reverseToLong( arr.metadata().shape );
			final int[] imgChunk = reverseToInt( arr.metadata().chunkShape() );
			final ReadOnlyCachedCellImgOptions opts = ReadOnlyCachedCellImgOptions.options().cellDimensions( imgChunk );
			cachedCellImgs[ level ] = new ReadOnlyCachedCellImgFactory()
					.create( imgShape, type, new ZarrJavaCellLoader<>( arr ), opts );
			volatileImgs[ level ] = VolatileViews.wrapAsVolatile( cachedCellImgs[ level ], sharedQueue );
		}

		final AxisCalibration[] axes = createAxisCalibrations( entry.axes, level0Scales );
		voxelDimensions = createVoxelDimensions( level0Scales, entry.axes );
		transforms = createTransforms( entry, numResolutionLevels, level0Scales );

		channelAxisIndex = imglibAxisIndex( entry.axes, AxisCalibration.C, numDimensions );
		zAxisIndex = imglibAxisIndex( entry.axes, AxisCalibration.Z, numDimensions );
		timeAxisIndex = imglibAxisIndex( entry.axes, AxisCalibration.T, numDimensions );

		// Try read Omero metadata
		final Omero omero = convertOmero( multiscaleImage.getOmeroMetadata() );
		channelLabels = Omero.buildChannelLabels( name, omero, numChannels );

		initSourceAndConverters();
	}

	private List< SourceAndConverter< T > > initSourceAndConverters( )
	{
		final List< SourceAndConverter< T > > sources = new ArrayList<>();
		for ( int channelNumber = 0; channelNumber < numChannels; channelNumber++ )
		{
			final RandomAccessibleInterval< V >[] channelsVolatile =
					ensureOrdered4dDimensions(
							extractChannel( volatileImgs, channelAxisIndex, channelNumber ),
							zAxisIndex >= 0, timeAxisIndex >= 0 );
			final RandomAccessibleInterval< T >[] channels =
					ensureOrdered4dDimensions(
							extractChannel( cachedCellImgs, channelAxisIndex, channelNumber ),
							zAxisIndex >= 0, timeAxisIndex >= 0 );

			final String channelLabel = channelLabels[ channelNumber ];
			final RandomAccessibleIntervalMipmapSource4D< V > source4DVolatile =
					new RandomAccessibleIntervalMipmapSource4D<>( channelsVolatile, volatileType, transforms, voxelDimensions, channelLabel,
							true );
			final RandomAccessibleIntervalMipmapSource4D< T > source4D =
					new RandomAccessibleIntervalMipmapSource4D<>( channels, type, transforms, voxelDimensions, channelLabel, true );

			final SourceAndConverter< T > sourceAndConverter = createSourceAndConverter( source4D, source4DVolatile );
			sources.add( sourceAndConverter );
			BigDataViewer.createConverterSetup( sourceAndConverter, channelNumber );
		}

		return sources;
	}

	/**
	 * If the channel dimension is present, hyper-slice it out at
	 * {@code channelNumber}; otherwise return the input arrays unchanged.
	 */
	private < R > RandomAccessibleInterval< R >[] extractChannel( final RandomAccessibleInterval< R >[] sourceImgs,
	                                                              final int channelAxisIndex, final int channelNumber )
	{
		final int numResolutionLevels = sourceImgs.length;
		final RandomAccessibleInterval< R >[] resultImgs = Cast.unchecked( new RandomAccessibleInterval[ numResolutionLevels ] );
		for ( int level = 0; level < numResolutionLevels; level++ )
		{
			resultImgs[ level ] = channelAxisIndex < 0
					? sourceImgs[ level ]
					: Views.hyperSlice( sourceImgs[ level ], channelAxisIndex, channelNumber );
		}
		return resultImgs;
	}

	/**
	 * Make sure images are 4D xyzt even if z and/or t are absent in the input
	 * tensor. A missing z is inserted before t; a missing t is appended.
	 */
	private < R > RandomAccessibleInterval< R >[] ensureOrdered4dDimensions( final RandomAccessibleInterval< R >[] sourceImgs,
	                                                                         final boolean zAxisPresent, final boolean timeAxisPresent )
	{
		final int numResolutionLevels = sourceImgs.length;
		for ( int level = 0; level < numResolutionLevels; level++ )
		{
			RandomAccessibleInterval< R > img = sourceImgs[ level ];
			if ( zAxisPresent )
			{
				if ( !timeAxisPresent ) // xyz → xyzt
					img = Views.addDimension( img, 0, 0 );
				// else xyzt already ordered correctly
			}
			else
			{
				if ( timeAxisPresent ) // xyt → xyzt: insert z before t
				{
					img = Views.addDimension( img, 0, 0 );
					img = Views.permute( img, 2, 3 );
				}
				else // xy → xyzt
				{
					img = Views.addDimension( img, 0, 0 );
					img = Views.addDimension( img, 0, 0 );
				}
			}
			sourceImgs[ level ] = img;
		}
		return sourceImgs;
	}

	private SourceAndConverter< T > createSourceAndConverter( final RandomAccessibleIntervalMipmapSource4D< T > source4D,
	                                                          final RandomAccessibleIntervalMipmapSource4D< V > source4DVolatile )
	{
		final Converter< V, ARGBType > converterVolatile = BigDataViewer.createConverterToARGB( volatileType );
		final Converter< T, ARGBType > converter = BigDataViewer.createConverterToARGB( type );
		final SourceAndConverter< V > sourceAndConverterVolatile =
				BigDataViewer.wrapWithTransformedSource( new SourceAndConverter<>( source4DVolatile, converterVolatile ) );
		return new SourceAndConverter<>( source4D, converter, sourceAndConverterVolatile );
	}

	private MultiscaleImage openMultiscaleImage()
	{
		final String scheme = inputUri.getScheme();
		Store store;
		if ( scheme == null || "file".equalsIgnoreCase( scheme ) )
			store = new FilesystemStore( Paths.get( inputUri ) );
		else if ( "http".equalsIgnoreCase( scheme ) || "https".equalsIgnoreCase( scheme ) )
			store = new HttpStore( inputUri.toString() );
		else
			throw new IllegalArgumentException( "Unsupported URI scheme '" + scheme + "' for OME-Zarr location: " + inputUri );
		return openMultiscaleImageFromHandle( store.resolve() );
	}

	private MultiscaleImage openMultiscaleImageFromHandle( final StoreHandle handle )
	{
		try
		{
			activeHandle = handle;
			return MultiscaleImage.open( handle );
		}
		catch ( ZarrException | IOException e )
		{
			checkForBioformats2rawLayout( handle );
			throw new NotAMultiscaleImageException( inputUri.toString(), e );
		}
	}

	private void checkForBioformats2rawLayout( final StoreHandle handle )
	{
		try
		{
			final Attributes attrs = Group.open( handle ).metadata().attributes();
			final Object ome = attrs.get( "ome" );
			if ( ome instanceof Map && ( ( Map< ?, ? > ) ome ).containsKey( "bioformats2raw.layout" ) )
				throw new MultiImageDatasetException( inputUri.toString() );
		}
		catch ( MultiImageDatasetException e )
		{
			throw e;
		}
		catch ( Exception e )
		{
			IJ.log( "Could not read group attributes from " + inputUri + ": " + e.getMessage() );
		}
	}

	/** Fallback dataset name when the multiscales entry has none. */
	private String defaultName()
	{
		if ( "file".equalsIgnoreCase( inputUri.getScheme() ) )
			return Paths.get( inputUri ).getFileName().toString();
		final String path = inputUri.getPath();
		if ( path == null || path.isEmpty() )
			return "";
		final String trimmed = path.endsWith( "/" ) ? path.substring( 0, path.length() - 1 ) : path;
		final int slash = trimmed.lastIndexOf( '/' );
		return slash >= 0 ? trimmed.substring( slash + 1 ) : trimmed;
	}

	private MultiscalesEntry readMultiscalesEntry( final MultiscaleImage multiscaleImage )
	{
		try
		{
			return multiscaleImage.getMultiscaleNode( 0 );
		}
		catch ( ZarrException | NullPointerException | IndexOutOfBoundsException e )
		{
			// NB: zarr-java declares only ZarrException on getMultiscaleNode, but in practice it leaks NullPointerException
			// when no multi scales entry is present
			// or an IndexOutOfBoundsException when the array is empty
			// surface those as a missing-metadata error rather than letting them
			// bubble up unhandled.
			if ( activeHandle != null )
				checkForBioformats2rawLayout( activeHandle );
			throw new NotAMultiscaleImageException( "No multiscale metadata at: " + inputUri, e );
		}
	}

	// ---------------------------------------------------------------------
	// Resolution level helpers
	// ---------------------------------------------------------------------

	private static int countResolutionLevels( final MultiscaleImage multiscaleImage )
	{
		try
		{
			return multiscaleImage.getScaleLevelCount();
		}
		catch ( ZarrException e )
		{
			return 1;
		}
	}

	private Array openLevel( final MultiscaleImage multiscaleImage, final int levelIndex )
	{
		try
		{
			return multiscaleImage.openScaleLevel( levelIndex );
		}
		catch ( IOException | ZarrException e )
		{
			throw new PyramidLevelAccessException( inputUri.toString(), levelIndex, e );
		}
	}

	// ---------------------------------------------------------------------
	// Axis / scale helpers
	// ---------------------------------------------------------------------

	private static int getDimSizeForAxis( final List< Axis > axes, final long[] zarrShape, final String axisName )
	{
		if ( axes == null )
			return 1;
		for ( int i = 0; i < axes.size(); i++ )
		{
			if ( axisName.equals( axes.get( i ).name ) )
				return ( int ) zarrShape[ i ];
		}
		return 1;
	}

	private static int zarrAxisIndex( final List< Axis > axes, final String axisName )
	{
		if ( axes == null )
			return -1;
		for ( int i = 0; i < axes.size(); i++ )
		{
			if ( axisName.equals( axes.get( i ).name ) )
				return i;
		}
		return -1;
	}

	private static int imglibAxisIndex( final List< Axis > axes, final String axisName, final int numDimensions )
	{
		final int zarrIndex = zarrAxisIndex( axes, axisName );
		return zarrIndex < 0 ? -1 : numDimensions - 1 - zarrIndex;
	}

	private static double[] getLevel0Scales( final MultiscalesEntry entry, final int numDimensions )
	{
		final double[] scales = findLevelScale( entry, 0 );
		if ( scales != null )
			return scales;
		final int n = entry.axes != null ? entry.axes.size() : numDimensions;
		final double[] fallback = new double[ n ];
		Arrays.fill( fallback, 1.0 );
		return fallback;
	}

	private static VoxelDimensions createVoxelDimensions( final double[] level0Scales, final List< Axis > zarrAxes )
	{
		if ( zarrAxes == null )
			return new FinalVoxelDimensions( "", 1.0, 1.0, 1.0 );
		final double xScale = scaleForNamedAxis( zarrAxes, level0Scales, AxisCalibration.X );
		final double yScale = scaleForNamedAxis( zarrAxes, level0Scales, AxisCalibration.Y );
		final double zScale = scaleForNamedAxis( zarrAxes, level0Scales, AxisCalibration.Z );
		return new FinalVoxelDimensions( spatialUnit( zarrAxes ), xScale, yScale, zScale );
	}

	private static double scaleForNamedAxis( final List< Axis > axes, final double[] scales, final String name )
	{
		final int idx = zarrAxisIndex( axes, name );
		return idx >= 0 ? scales[ idx ] : 1.0;
	}

	/**
	 * Returns the unit attached to the last x/y/z axis encountered. OME-Zarr
	 * spatial axes share a single unit in well-formed datasets, so this
	 * collapses to "the spatial unit"; the original loop happened to write
	 * it last-wins, and this preserves that behavior.
	 */
	private static String spatialUnit( final List< Axis > axes )
	{
		String unit = "";
		for ( final Axis axis : axes )
		{
			if ( AxisCalibration.X.equals( axis.name ) || AxisCalibration.Y.equals( axis.name ) || AxisCalibration.Z.equals( axis.name ) )
				unit = axis.unit == null ? "" : axis.unit;
		}
		return unit;
	}

	private static AffineTransform3D[] createTransforms( final MultiscalesEntry entry,
			final int numResolutionLevels, final double[] level0Scales )
	{
		final int[] spatialZarrIdx = new int[] {
				zarrAxisIndex( entry.axes, AxisCalibration.X ),
				zarrAxisIndex( entry.axes, AxisCalibration.Y ),
				zarrAxisIndex( entry.axes, AxisCalibration.Z )
		};
		final AffineTransform3D[] tr = new AffineTransform3D[ numResolutionLevels ];
		for ( int level = 0; level < numResolutionLevels; level++ )
		{
			final double[] scales = computeLevelScale( entry, level, level0Scales, spatialZarrIdx );
			final AffineTransform3D t = new AffineTransform3D();
			t.set( scales[ 0 ], 0, 0 );
			t.set( scales[ 1 ], 1, 1 );
			t.set( scales[ 2 ], 2, 2 );
			tr[ level ] = t;
		}
		return tr;
	}

	private static double[] computeLevelScale( final MultiscalesEntry entry, final int level,
			final double[] level0Scales, final int[] spatialZarrIdx )
	{
		final double[] levelScale = findLevelScale( entry, level );
		if ( levelScale == null )
			return fallbackSpatialScale( level0Scales, spatialZarrIdx );

		final double[] scales = new double[ 3 ];
		for ( int d = 0; d < 3; d++ )
		{
			final int zi = spatialZarrIdx[ d ];
			if ( zi >= 0 && zi < levelScale.length )
				scales[ d ] = levelScale[ zi ];
			else
				scales[ d ] = fallbackScaleAtAxis( level0Scales, zi );
		}
		return scales;
	}

	/**
	 * Returns the resolved scale array of the first
	 * {@link ScaleCoordinateTransformation} at {@code level} whose
	 * {@code scale} field is non-null. Returns {@code null} when the level
	 * doesn't exist or has no usable scale transformation. Returning the
	 * array directly (instead of the library type with a nullable
	 * {@code scale} field) keeps null-tracking local to this method, so
	 * callers don't have to repeat the {@code scaleCt.scale != null} check.
	 * OME-Zarr datasets carry at most one scale transformation per level,
	 * so "first usable one" is observably equivalent to "first scale ct,
	 * null-check at the call site".
	 * <p>
	 * Sonar's {@code S1168} ("return an empty array instead of null") does
	 * not apply: callers branch on the absence of a scale transformation
	 * (and build a length-correct fallback in that branch); an empty array
	 * would silently take the "use it" path and produce a zero-extent
	 * dataset.
	 */
	@SuppressWarnings( "java:S1168" )
	private static double[] findLevelScale( final MultiscalesEntry entry, final int level )
	{
		if ( entry.datasets == null || entry.datasets.size() <= level )
			return null;
		final dev.zarr.zarrjava.experimental.ome.metadata.Dataset ds = entry.datasets.get( level );
		if ( ds.coordinateTransformations == null )
			return null;
		for ( final CoordinateTransformation ct : ds.coordinateTransformations )
		{
			if ( ct instanceof ScaleCoordinateTransformation )
			{
				final ScaleCoordinateTransformation scaleCt = ( ScaleCoordinateTransformation ) ct;
				if ( scaleCt.scale != null )
					return toDoubleArray( scaleCt.scale );
			}
		}
		return null;
	}

	private static double[] toDoubleArray( final List< Double > values )
	{
		final double[] out = new double[ values.size() ];
		for ( int i = 0; i < out.length; i++ )
			out[ i ] = values.get( i );
		return out;
	}

	private static double[] fallbackSpatialScale( final double[] level0Scales, final int[] spatialZarrIdx )
	{
		final double[] scales = new double[ 3 ];
		for ( int d = 0; d < 3; d++ )
			scales[ d ] = fallbackScaleAtAxis( level0Scales, spatialZarrIdx[ d ] );
		return scales;
	}

	private static double fallbackScaleAtAxis( final double[] level0Scales, final int zarrIndex )
	{
		return zarrIndex >= 0 && zarrIndex < level0Scales.length ? level0Scales[ zarrIndex ] : 1.0;
	}

	private static AxisCalibration[] createAxisCalibrations( final List< Axis > zarrAxes, final double[] level0Scales )
	{
		if ( zarrAxes == null )
			return new AxisCalibration[ 0 ];
		final int n = zarrAxes.size();
		final AxisCalibration[] result = new AxisCalibration[ n ];
		for ( int zarrDim = 0; zarrDim < n; zarrDim++ )
		{
			final int imgDim = n - 1 - zarrDim;
			final Axis axis = zarrAxes.get( zarrDim );
			final String unit = axis.unit != null ? axis.unit : "";
			result[ imgDim ] = new AxisCalibration( axis.name, unit, level0Scales[ zarrDim ] );
		}
		return result;
	}

	// ---------------------------------------------------------------------
	// Type mapping / utility
	// ---------------------------------------------------------------------

	@SuppressWarnings( "unchecked" )
	private static < T extends NativeType< T > & RealType< T > > T typeForZarrDataType( final ucar.ma2.DataType dt )
	{
		if ( dt == ucar.ma2.DataType.FLOAT )
			return ( T ) new FloatType();
		if ( dt == ucar.ma2.DataType.DOUBLE )
			return ( T ) new DoubleType();
		if ( dt == ucar.ma2.DataType.BYTE )
			return ( T ) new ByteType();
		if ( dt == ucar.ma2.DataType.UBYTE )
			return ( T ) new UnsignedByteType();
		if ( dt == ucar.ma2.DataType.SHORT )
			return ( T ) new ShortType();
		if ( dt == ucar.ma2.DataType.USHORT )
			return ( T ) new UnsignedShortType();
		if ( dt == ucar.ma2.DataType.INT )
			return ( T ) new IntType();
		if ( dt == ucar.ma2.DataType.UINT )
			return ( T ) new UnsignedIntType();
		if ( dt == ucar.ma2.DataType.LONG )
			return ( T ) new LongType();
		if ( dt == ucar.ma2.DataType.ULONG )
			return ( T ) new UnsignedLongType();
		throw new IllegalArgumentException( "Unsupported zarr data type: " + dt );
	}

	private static long[] reverseToLong( final long[] arr )
	{
		final long[] out = new long[ arr.length ];
		for ( int i = 0; i < arr.length; i++ )
			out[ i ] = arr[ arr.length - 1 - i ];
		return out;
	}

	private static int[] reverseToInt( final int[] arr )
	{
		final int[] out = new int[ arr.length ];
		for ( int i = 0; i < arr.length; i++ )
			out[ i ] = arr[ arr.length - 1 - i ];
		return out;
	}
}
