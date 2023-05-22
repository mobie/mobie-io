package org.embl.mobie.io.util;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import com.moandjiezana.toml.Toml;
import ij.ImagePlus;
import ij.VirtualStack;
import ij.measure.Calibration;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.embl.mobie.io.SpimDataOpener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
		final Toml toml = new Toml().read( new File( tomlImagePath ) );
		final Double scaleXY = toml.getDouble( "scale_xy" );
		final String spatialUnit = toml.getString( "unit_xy" );
		final Double scaleT = toml.getDouble( "scale_t" );
		final String timeUnit = toml.getString( "unit_t" );
		final String path = toml.getString( "path" );

		final List< String > groups = IOHelper.getNamedGroups( path );

		return null;
//		VirtualStack virtualStack = null;
//
//		final Map< TPosition, Map< ZPosition, String > > paths = site.getPaths();
//
//		final ArrayList< TPosition > tPositions = new ArrayList<>( paths.keySet() );
//		Collections.sort( tPositions );
//		int nT = tPositions.size();
//		int nZ = 1;
//		for ( TPosition t : tPositions )
//		{
//			final Set< ZPosition > zPositions = paths.get( t ).keySet();
//			nZ = zPositions.size();
//			for ( ZPosition z : zPositions )
//			{
//				if ( virtualStack == null )
//				{
//					final int[] dimensions = site.getDimensions();
//					virtualStack = new VirtualStack( dimensions[ 0 ], dimensions[ 1 ], null, "" );
//				}
//
//				virtualStack.addSlice( paths.get( t ).get( z ) );
//			}
//		}
//
//		final ImagePlus imagePlus = new ImagePlus( site.getName(), virtualStack );
//
//		final Calibration calibration = new Calibration();
//		calibration.pixelWidth = scaleXY;
//		calibration.pixelHeight = scaleXY;
//		calibration.frameInterval = scaleT;
//		calibration.setUnit( spatialUnit );
//		calibration.setTimeUnit( timeUnit );
//		imagePlus.setCalibration( calibration );
//
//		// TODO: is could be zSlices!
//		imagePlus.setDimensions( 1, nZ, nT );
//
//		final AbstractSpimData< ? > spimData = ImagePlusToSpimData.getSpimData( imagePlus );
//		SpimDataOpener.setSharedQueue( sharedQueue, spimData );
//
//		return spimData;
	}
}
