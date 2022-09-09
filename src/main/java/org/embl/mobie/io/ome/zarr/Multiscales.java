package org.embl.mobie.io.ome.zarr;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.embl.mobie.io.ome.zarr.util.AxesTypes;
import org.embl.mobie.io.ome.zarr.util.OMEZarrAxes;
import org.embl.mobie.io.ome.zarr.util.ZarrAxis;

import java.util.ArrayList;
import java.util.List;

/**
 * Copy from {@code org.embl.mobie.io.ome.zarr.util.OmeZarrMultiscales}
 *
 */
public class Multiscales
{
    // key in json for multiscales
    public static final String MULTI_SCALE_KEY = "multiscales";

    // Serialisation
    public String version;
    public String name;
    public String type;
    public Axes[] axes; // from v0.4+ within JSON
    public Dataset[] datasets;
    public CoordinateTransformations[] coordinateTransformations;

    public Multiscales() {
    }

    private void generateDatasets(VoxelDimensions voxelDimensions, double frameInterval, double[][] resolutions) {

        Dataset[] datasets = new Dataset[resolutions.length];
        for (int i = 0; i < resolutions.length; i++) {
            Dataset dataset = new Dataset();

            CoordinateTransformations coordinateTransformations = new CoordinateTransformations();
            coordinateTransformations.scale = getScale(voxelDimensions, frameInterval, resolutions[i]);
            coordinateTransformations.type = "scale";

            dataset.path = "s" + i;
            dataset.coordinateTransformations = new CoordinateTransformations[]{coordinateTransformations};
            datasets[i] = dataset;
        }
        this.datasets = datasets;
    }

    private double[] getScale(VoxelDimensions voxelDimensions, double frameInterval, double[] xyzScale) {
        int nDimensions = zarrAxisList.size();
        double[] scale = new double[nDimensions];
        if (axes.timeIndex() != -1) {
            scale[axes.timeIndex()] = frameInterval;
        }

        if (axes.channelIndex() != -1) {
            scale[axes.channelIndex()] = 1;
        }

        for (int i = 0; i < 3; i++) {
            double dimension = voxelDimensions.dimension(i) * xyzScale[i];
            scale[nDimensions - (i + 1)] = dimension;
        }

        return scale;
    }

    public static class Dataset {
        public String path;
        public CoordinateTransformations[] coordinateTransformations;
    }

    public static class CoordinateTransformations {
        public String type;
        public double[] scale;
        public double[] translation;
        public String path;
    }

    public static class Axes {
        public String name;
        public String type;
        public String unit;
    }

    /**
     * "Manually" set some fields from the JSON.
     * This is necessary to support the
     * different versions of OME-Zarr.
     *
     * TODO: Do this via a JSONAdapter?
     *
     * @param multiscales The multiscales JSON metadata.
     */
    public void applyVersionFixes( JsonElement multiscales ) {
        final JsonElement jsonElement = multiscales.getAsJsonArray().get( 0 );
        String version = jsonElement.getAsJsonObject().get("version").getAsString();
        if ( version.equals("0.3") ) {
            JsonElement axes = jsonElement.getAsJsonObject().get("axes");
            // TODO
            //   - populate Axes[]
            //   - populate coordinateTransformations[]
            throw new RuntimeException("Parsing version 0.3 not yet implemented.");
        } else if ( version.equals("0.4") ) {
            // This should just work automatically
        } else {
            JsonElement axes = jsonElement.getAsJsonObject().get("axes");
            // TODO
            //   - populate Axes[]
            //   - populate coordinateTransformations[]
            throw new RuntimeException("Parsing version "+ version + " is not yet implemented.");
        }
    }

    private void setAxes( JsonElement axes )
    {
        // TODO
    }


}
