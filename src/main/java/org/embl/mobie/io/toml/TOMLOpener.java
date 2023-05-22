package org.embl.mobie.io.toml;

import bdv.cache.SharedQueue;
import ch.epfl.biop.bdv.img.imageplus.ImagePlusToSpimData;
import com.moandjiezana.toml.Toml;
import ij.IJ;
import ij.ImagePlus;
import ij.VirtualStack;
import ij.measure.Calibration;
import mpicbg.spim.data.generic.AbstractSpimData;
import org.embl.mobie.io.SpimDataOpener;
import org.embl.mobie.io.util.IOHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TOMLOpener
{
	private final String imageName;
	private final String tomlImagePath;

	public TOMLOpener( String imageName, String tomlImagePath )
	{
		this.imageName = imageName;
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

	public AbstractSpimData< ? > asSpimData( SharedQueue sharedQueue )
	{
		final AbstractSpimData< ? > spimData = asSpimData();
		SpimDataOpener.setSharedQueue( sharedQueue, spimData );
		return spimData;
	}

	public AbstractSpimData< ? > asSpimData()
	{
		final ImagePlus imagePlus = asImagePlus();
		final AbstractSpimData< ? > spimData = ImagePlusToSpimData.getSpimData( imagePlus );
		return spimData;
	}
}
