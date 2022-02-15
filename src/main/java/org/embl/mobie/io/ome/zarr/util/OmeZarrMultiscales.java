package org.embl.mobie.io.ome.zarr.util;

import bdv.export.ExportMipmapInfo;
import com.google.gson.annotations.SerializedName;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.janelia.saalfeldlab.n5.N5Reader;

import java.util.List;
import java.util.Map;

public class OmeZarrMultiscales {

    // key in json for multiscales
    public static final String MULTI_SCALE_KEY = "multiscales";

    public transient ZarrAxes axes;
    @SerializedName(value = "axes")
    public List<ZarrAxis> zarrAxisList;
    public Dataset[] datasets;
    public String name;
    public String type;
    public String version;
    public CoordinateTransformations[] coordinateTransformations;

    public OmeZarrMultiscales() {
    }

    public OmeZarrMultiscales( ZarrAxes axes, String name, String type, String version,
                              VoxelDimensions voxelDimensions, double[][] resolutions, String timeUnit ) {
        this.version = version;
        this.name = name;
        this.type = type;
        this.axes = axes;
        this.zarrAxisList = axes.toAxesList( voxelDimensions.unit(), timeUnit );
        generateDatasets( voxelDimensions, resolutions );
    }

    private void generateDatasets( VoxelDimensions voxelDimensions, double[][] resolutions ) {

        Dataset[] datasets = new Dataset[resolutions.length];
        for (int i = 0; i < resolutions.length; i++) {
            Dataset dataset = new Dataset();

            CoordinateTransformations coordinateTransformations = new CoordinateTransformations();
            // TODO - sort adding time
            coordinateTransformations.scale = getScale( voxelDimensions, resolutions[i]);
            coordinateTransformations.type = "scale";

            dataset.path = "s" + i;
            dataset.coordinateTransformations = new CoordinateTransformations[]{coordinateTransformations};
            datasets[i] = dataset;
        }
        this.datasets = datasets;
    }

    private double[] getScale( VoxelDimensions voxelDimensions, double[] xyzScale ){
        int nDimensions = zarrAxisList.size();
        double[] scale = new double[nDimensions];
        if ( axes.timeIndex() != -1 ) {
            // TODO - put real time scale here
            scale[axes.timeIndex()] = 1;
        }

        if ( axes.channelIndex() != -1 ) {
            scale[axes.channelIndex()] = 1;
        }

        for ( int i = 0; i<3; i++ ) {
            double dimension = voxelDimensions.dimension(i) * xyzScale[i];
            scale[nDimensions - (i+1)] = dimension;
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
}
