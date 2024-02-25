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
package org.embl.mobie.io.toml;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import com.moandjiezana.toml.Toml;
import ij.IJ;
import ij.ImagePlus;
import ij.VirtualStack;
import ij.measure.Calibration;
import org.embl.mobie.io.util.IOHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TOMLOpener
{
	private final String tomlImagePath;

	public TOMLOpener( String tomlImagePath )
	{
		this.tomlImagePath = tomlImagePath;
	}

	public ImagePlus asImagePlus()
	{
		final File tomlFile = new File( tomlImagePath );
		final Toml toml = new Toml().read( tomlFile );
		final Double scaleXY = toml.getDouble( "scale_xy" );
		final String spatialUnit = toml.getString( "unit_xy" );
		final Double scaleT = toml.getDouble( "scale_t" );
		final String timeUnit = toml.getString( "unit_t" );
		final String path = toml.getString( "path" );
		final String imageName = tomlFile.getName().replace( ".image.toml", "" );

		final List< String > groups = IOHelper.getNamedGroups( path );

		final Map< TPosition, Map< ZPosition, String > > frameSlicePaths = new HashMap<>();

		if ( groups.contains( "frame" ) && ! groups.contains( "slice" ) )
		{
			List< String > framePaths = IOHelper.getPaths( tomlFile.getParent(), path, 999 );
			for ( String framePath : framePaths )
			{
				final TPosition tPosition = new TPosition( framePath );
				final Map< ZPosition, String > slicePaths = new HashMap<>();
				slicePaths.put( new ZPosition( "0" ), framePath );
				frameSlicePaths.put( tPosition, slicePaths );
			}
		}
		else
		{
			throw new UnsupportedOperationException("TOML opening of path not yet supported: " + path );
		}

		VirtualStack virtualStack = null;

		final ArrayList< TPosition > tPositions = new ArrayList<>( frameSlicePaths.keySet() );
		Collections.sort( tPositions );
		int nT = tPositions.size();
		int nZ = 1;
		for ( TPosition t : tPositions )
		{
			final Set< ZPosition > zPositions = frameSlicePaths.get( t ).keySet();
			nZ = zPositions.size();
			for ( ZPosition z : zPositions )
			{
				if ( virtualStack == null )
				{
					final String imagePath = frameSlicePaths.get( t ).get( z );
					final ImagePlus imp = IJ.openImage( imagePath );
					virtualStack = new VirtualStack( imp.getWidth(), imp.getHeight(), null, "" );
				}

				virtualStack.addSlice( frameSlicePaths.get( t ).get( z ) );
			}
		}

		final ImagePlus imagePlus = new ImagePlus( imageName, virtualStack );

		final Calibration calibration = new Calibration();
		calibration.pixelWidth = scaleXY;
		calibration.pixelHeight = scaleXY;
		calibration.frameInterval = scaleT;
		calibration.setUnit( spatialUnit );
		calibration.setTimeUnit( timeUnit );
		imagePlus.setCalibration( calibration );
		imagePlus.setDimensions( 1, nZ, nT );

		return imagePlus;
	}

//	public AbstractSpimData< ? > asSpimData( SharedQueue sharedQueue )
//	{
//		final AbstractSpimData< ? > spimData = asSpimData();
//		SpimDataOpener.setSharedQueue( sharedQueue, spimData );
//		return spimData;
//	}
//
//	public AbstractSpimData< ? > asSpimData()
//	{
//		final ImagePlus imagePlus = asImagePlus();
//		final AbstractSpimData< ? > spimData = ImagePlusToSpimData.getSpimData( imagePlus );
//		return spimData;
//	}
}
