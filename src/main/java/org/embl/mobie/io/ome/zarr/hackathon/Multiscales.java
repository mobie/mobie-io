package org.embl.mobie.io.ome.zarr.hackathon;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.concatenate.PreConcatenable;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.Scale;
import net.imglib2.realtransform.Scale2D;
import net.imglib2.realtransform.Scale3D;
import net.imglib2.realtransform.Translation;
import net.imglib2.realtransform.Translation2D;
import net.imglib2.realtransform.Translation3D;

import java.util.Arrays;
import java.util.List;

import org.embl.mobie.io.ome.zarr.hackathon.Multiscales.Dataset;

import org.janelia.saalfeldlab.n5.metadata.SpatialMetadata;
import org.janelia.saalfeldlab.n5.metadata.SpatialMetadataGroup;

/**
 * Copy from {@code org.embl.mobie.io.ome.zarr.util.OmeZarrMultiscales}
 *
 */
public class Multiscales implements SpatialMetadataGroup<Dataset>
{
    // key in json for multiscales
    public static final String MULTI_SCALE_KEY = "multiscales";

    public transient String path;

    // Serialisation
    private String version;
    private String name;
    private String type;
    private Axis[] axes; // from v0.4+ within JSON
    private Dataset[] datasets;
    private CoordinateTransformations[] coordinateTransformations; // from v0.4+ within JSON

    // Runtime

    // Simply contains the {@codeAxes[] axes}
    // but in reversed order to accommodate
    // the Java array ordering of the image data.
    private List< Axis > axisList;
    private int numDimensions;

    public Multiscales() {
    }

	public Multiscales(String version, String name, String type, 
			Axis[] axes, Dataset[] datasets, CoordinateTransformations[] coordinateTransformations)
	{
		this.version = version;
		this.name = name;
		this.type = type;
		this.axes = axes;
		this.datasets = datasets;
		this.coordinateTransformations = coordinateTransformations;
	}

    public static class Dataset implements SpatialMetadata {
        public String path;
        public CoordinateTransformations[] coordinateTransformations;
        public transient String unit = ""; // default empty string means "unitless"

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public AffineGet spatialTransform() {
			return buildTransform();
		}

		/**
		 * Make an {@link AffineGet} from a sequence of coordinate transformations
		 * (scales and translations).
		 * 
		 * @param <S> a temporary type 
		 * @return the transformation
		 */
		private <S extends AffineGet & PreConcatenable<AffineGet>> AffineGet buildTransform() {
			if( coordinateTransformations.length == 0 )
				return new Scale(1, 1, 1, 1, 1 );
			else if( coordinateTransformations.length == 1 )
				return coordinateTransformations[0].getTransform();
			else
			{
				final AffineGet t0 = coordinateTransformations[0].getTransform();
				final int nd = t0.numSourceDimensions();

				S out;
				if( nd == 3 )
					out = (S)new AffineTransform3D();
				else if( nd == 3 )
					out = (S)new AffineTransform2D();
				else
					out = (S)new AffineTransform(nd);

				out.preConcatenate(t0);
				for( int i = 0; i < coordinateTransformations.length; i++ )
					out.preConcatenate( coordinateTransformations[i].getTransform());

				return out;
			}
		}

		@Override
		public String unit() {
			return unit;
		}
    }

    public static class CoordinateTransformations {

		/*
		 * Note, the 0.4 spec requires that only one of scale or translation be present,
		 * and that if type == "scale" then scale is not null, and that if type ==
		 * "translation" then translation is not null,
		 */
        public String type;
        public double[] scale;
        public double[] translation;
        public String path;

		/**
		 * Returns an imglib2 {@link AffineGet} that this object represents.
		 *
		 * @return the transformation
		 */
		public AffineGet getTransform() {
			if (translation == null) {
				if (scale.length == 3)
					return new Scale3D(scale);
				else if (scale.length == 2)
					return new Scale2D(scale);
				else
					return new Scale(scale);
			} else if (scale == null) {
				if (translation.length == 3)
					return new Translation3D(translation);
				else if (scale.length == 2)
					return new Translation2D(translation);
				else
					return new Translation(translation);
			} else {
				// should not happen for valid datasets.
				return null;
			}
		}
    }

    public static class Axis
    {
        public static final String CHANNEL_TYPE = "channel";
        public static final String TIME_TYPE = "time";
        public static final String SPATIAL_TYPE = "space";

        public static final String X_AXIS_NAME = "x";
        public static final String Y_AXIS_NAME = "y";
        public static final String Z_AXIS_NAME = "z";

        public String name;
        public String type;
        public String unit;

		public Axis(String name, String type, String unit)
		{
			this.name = name;
			this.type = type;
			this.unit = unit;
		}
    }

    public void init()
    {
        axisList = Lists.reverse( Arrays.asList( axes ) );
        numDimensions = axisList.size();

        // TODO I'm not sure if this is correct. double check later -John
        for( int i = 0; i < numDimensions; i++ )
			datasets[i].unit = axisList.get(i).unit;
    }

    // TODO Can this be done with a JSONAdapter ?
    public void applyVersionFixes( JsonElement multiscales )
    {
        String version = multiscales.getAsJsonObject().get("version").getAsString();
        if ( version.equals("0.3") ) {
            JsonElement axes = multiscales.getAsJsonObject().get("axes");
            // FIXME
            //   - populate Axes[]
            //   - populate coordinateTransformations[]
            throw new RuntimeException("Parsing version 0.3 not yet implemented.");
        } else if ( version.equals("0.4") ) {
            // This should just work automatically
        } else {
            JsonElement axes = multiscales.getAsJsonObject().get("axes");
            // FIXME
            //   - populate Axes[]
            //   - populate coordinateTransformations[]
            throw new RuntimeException("Parsing version "+ version + " is not yet implemented.");
        }
    }

    public int getChannelAxisIndex()
    {
        for ( int d = 0; d < numDimensions; d++ )
            if ( axisList.get( d ).type.equals( Axis.CHANNEL_TYPE ) )
                return d;
        return -1;
    }

    public int getTimePointAxisIndex()
    {
        for ( int d = 0; d < numDimensions; d++ )
            if ( axisList.get( d ).type.equals( Axis.TIME_TYPE ) )
                return d;
        return -1;
    }

    public int getSpatialAxisIndex( String axisName )
    {
        for ( int d = 0; d < numDimensions; d++ )
            if ( axisList.get( d ).type.equals( Axis.SPATIAL_TYPE )
                 && axisList.get( d ).name.equals( axisName ) )
                return d;
        return -1;
    }

    public List< Axis > getAxes()
    {
        return axisList;
    }

    public CoordinateTransformations[] getCoordinateTransformations()
    {
        return coordinateTransformations;
    }

    public Dataset[] getDatasets()
    {
        return datasets;
    }

    public int numDimensions()
    {
        return numDimensions;
    }

	@Override
	public String[] getPaths() {
		return Arrays.stream( datasets ).map( x -> x.path ).toArray( String[]::new );
	}

	@Override
	public Dataset[] getChildrenMetadata() {
		return getDatasets();
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String[] units() {
		return axisList.stream().map( x -> x.unit ).toArray( String[]::new );
	}

}
