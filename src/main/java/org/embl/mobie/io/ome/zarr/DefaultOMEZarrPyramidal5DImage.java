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
import bdv.util.AxisOrder;
import bdv.viewer.SourceAndConverter;
import com.bitplane.xt.util.ColorTableUtils;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.EuclideanSpace;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import org.embl.mobie.io.ome.zarr.util.OMEZarrAxes;
import org.embl.mobie.io.ome.zarr.util.OmeZarrMultiscales;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * An OME-Zarr backed pyramidal 5D image
 * that can be visualised in ImageJ in various ways.
 *
 * @param <T> Type of the pixels
 * @param <V> Volatile type of the pixels
 */
public class DefaultOMEZarrPyramidal5DImage< T extends NativeType< T > & RealType< T >, V extends Volatile< T > & NativeType< V > & RealType< V > > implements EuclideanSpace, Pyramidal5DImage< T >
{
	/**
	 * The scijava context. This is needed (only) for creating {@link #ijDataset}.
	 */
	private final Context context;

	private final String name;

	private final PyramidalOMEZarrArray< T, V > pyramidalArray;

	private final int numChannels;

	private final int numTimePoints;

	private int numResolutions;

	private ImgPlus< T > imgPlus;

	private Dataset ijDataset;

	private List< SourceAndConverter< T > > sources;

	private SpimData spimData;
	private OMEZarrAxes omeZarrAxes;

	/**
	 * Build a dataset from a single {@code PyramidalOMEZarrArray},
	 * which MUST only contains subset of the axes: X,Y,Z,C,T
	 *
	 * @param context The SciJava context for building the SciJava dataset
	 * @param pyramidalArray The array containing the image all data.
	 * @throws Error
	 */
	DefaultOMEZarrPyramidal5DImage(
			final Context context,
			final String name,
			final PyramidalOMEZarrArray< T, V > pyramidalArray ) throws Error
	{
		this.context = context;
		this.name = name;
		this.pyramidalArray = pyramidalArray;

		omeZarrAxes = pyramidalArray.getAxes();
		numResolutions = pyramidalArray.numResolutions();;
		final long[] dimensions = pyramidalArray.dimensions();
		numChannels = omeZarrAxes.hasChannels() ? ( int ) dimensions[ omeZarrAxes.channelIndex() ] : 1;
		numTimePoints = omeZarrAxes.hasTimepoints() ? ( int ) dimensions[ omeZarrAxes.timeIndex() ] : 1;
	}


	/**
	 * Get an {@code ImgPlus} wrapping the full resolution image (see {@link
	 * #asImg}). Metadata and color tables are set up according to Imaris (at
	 * the time of construction of this {@code ImarisDataset}).
	 */
	public ImgPlus< T > asImgPlus()
	{
		initImgPlus();
		return imgPlus;
	}

	@Override
	public Dataset asDataset()
	{
		initImgPlus();

		synchronized ( imgPlus )
		{
			if ( ijDataset == null )
			{
				final DatasetService datasetService = context.getService( DatasetService.class );
				ijDataset = datasetService.create( imgPlus );
				ijDataset.setName( imgPlus.getName() );
				ijDataset.setRGBMerged( false );
			}
			return ijDataset;
		}
	}


	@Override
	public List< SourceAndConverter< T > > asSources()
	{
		return sources;
	}

	@Override
	public SpimData asSpimData()
	{
		return spimData;
	}

	@Override
	public int numChannels()
	{
		return numChannels;
	}

	@Override
	public VoxelDimensions voxelDimensions()
	{
		return null;
	}

	private void initImgPlus()
	{
		if ( imgPlus != null ) return;

		imgPlus = new ImgPlus<>( pyramidalArray.getImg( 0 ) );
		imgPlus.setName( getName() );
		updateImpAxes();
		updateImpColorTables();
		updateImpChannelMinMax();
	}

	/**
	 * Create/update calibrated axes for ImgPlus.
	 */
	private void updateImpAxes()
	{
		OmeZarrMultiscales multiscales = setupToMultiscale.get(setupId);

		if (multiscales.datasets[datasetId].coordinateTransformations != null && zarrAxesList != null) {
			double[] scale = getXYZScale(multiscales.datasets[datasetId].coordinateTransformations[0]);
			// get unit of last dimension, under assumption this is the X axis (i.e. a space axis)
			String unit = zarrAxesList.get(zarrAxesList.size() - 1).getUnit();

			if (scale != null && unit != null) {
				if ( OMEZarrAxes.is2D()) {
					return new FinalVoxelDimensions(unit, new double[]{scale[0], scale[1], 1.0});
				} else {
					return new FinalVoxelDimensions(unit, scale);
				}
			}
		}

		omeZarrAxes.toAxesList(  )
		final ArrayList< CalibratedAxis > axes = new ArrayList<>();
		axes.add( new DefaultLinearAxis( Axes.X, calib.unit(), calib.voxelSize( 0 ) ) );
		axes.add( new DefaultLinearAxis( Axes.Y, calib.unit(), calib.voxelSize( 1 ) ) );

		final AxisOrder axisOrder = datasetDimensions.getAxisOrder();
		if ( axisOrder.hasZ() )
			axes.add( new DefaultLinearAxis( Axes.Z, calib.unit(), calib.voxelSize( 2 ) ) );
		if ( axisOrder.hasChannels() )
			axes.add( new DefaultLinearAxis( Axes.CHANNEL ) );
		if ( axisOrder.hasTimepoints() )
			axes.add( new DefaultLinearAxis( Axes.TIME ) );

		for ( int i = 0; i < axes.size(); ++i )
			imgPlus.setAxis( axes.get( i ), i );
	}

	@Override
	public int numDimensions()
	{
		return pyramidalArray.numDimensions();
	}

	/**
	 * Get the number of levels in the resolution pyramid.
	 */
	public int numResolutions()
	{
		return numResolutions;
	}

	/**
	 * Get the number timepoints.
	 */
	@Override
	public int numTimePoints()
	{
		return numTimePoints;
	}

	/**
	 * Get an instance of the pixel type.
	 */
	@Override
	public T getType()
	{
		return pyramidalArray.getType();
	}

	/**
	 * Get the size of the underlying
	 * 5D Imaris dataset and the mapping to dimensions of the ImgLib2
	 * representation.
	 */
	public DatasetDimensions getDatasetDimensions()
	{
		return datasetDimensions;
	}

	/**
	 * Get the physical calibration: unit, voxel size, and min in XYZ.
	 */
	public DatasetCalibration getCalibration()
	{
		return calib.copy();
	}

	/**
	 * Get the base color of a channel.
	 *
	 * @param channel index of the channel
	 * @return channel color
	 */
	public ARGBType getChannelColor( final int channel ) throws Error
	{
		return ColorTableUtils.getChannelColor( zArrayPath, channel );
	}

	@Override
	public String getName()
	{
		return name;
	}
}
