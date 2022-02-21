package org.embl.mobie.io.ome.zarr.util;

import mpicbg.spim.data.sequence.VoxelDimensions;

import java.util.List;

public class OmeZarrMultiscales {

    // key in json for multiscales
    public static final String MULTI_SCALE_KEY = "multiscales";

    public transient ZarrAxes axes;
    public List<ZarrAxis> zarrAxisList;
    public Dataset[] datasets;
    public String name;
    public String type;
    public String version;
    public CoordinateTransformations[] coordinateTransformations;

    public OmeZarrMultiscales() {
    }

    public OmeZarrMultiscales( ZarrAxes axes, String name, String type, String version,
                              VoxelDimensions voxelDimensions, double[][] resolutions, String timeUnit,
                               double frameInterval ) {
        this.version = version;
        this.name = name;
        this.type = type;
        this.axes = axes;
        this.zarrAxisList = axes.toAxesList( voxelDimensions.unit(), timeUnit );
        generateDatasets( voxelDimensions, frameInterval, resolutions );
    }

    private void generateDatasets( VoxelDimensions voxelDimensions, double frameInterval, double[][] resolutions ) {

        Dataset[] datasets = new Dataset[resolutions.length];
        for (int i = 0; i < resolutions.length; i++) {
            Dataset dataset = new Dataset();

            CoordinateTransformations coordinateTransformations = new CoordinateTransformations();
            coordinateTransformations.scale = getScale( voxelDimensions, frameInterval, resolutions[i]);
            coordinateTransformations.type = "scale";

            dataset.path = "s" + i;
            dataset.coordinateTransformations = new CoordinateTransformations[]{coordinateTransformations};
            datasets[i] = dataset;
        }
        this.datasets = datasets;
    }

    private double[] getScale( VoxelDimensions voxelDimensions, double frameInterval, double[] xyzScale ){
        int nDimensions = zarrAxisList.size();
        double[] scale = new double[nDimensions];
        if ( axes.timeIndex() != -1 ) {
            scale[axes.timeIndex()] = frameInterval;
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
