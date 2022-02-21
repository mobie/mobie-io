package org.embl.mobie.io.ome.zarr.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import mpicbg.spim.data.sequence.VoxelDimensions;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.HashMap;
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

    public static HashMap<String, JsonElement> getMultiScalesMap(OmeZarrMultiscales[] omeZarrMultiscalesArray){
        HashMap<String, JsonElement> map = new HashMap<>();
        JsonObjectBuilder factory = Json.createObjectBuilder();
        Gson gson = new Gson();
        OmeZarrMultiscales omeZarrMultiscales = omeZarrMultiscalesArray[0];
        String datasets = gson.toJson(omeZarrMultiscales.datasets);
        String coordinateTransformations = gson.toJson(omeZarrMultiscales.coordinateTransformations);
        factory
                .add("axes", createJsonArrayFromList(omeZarrMultiscales.zarrAxisList))
                .add("datasets", createJsonArrayFromList(omeZarrMultiscales.datasets))
                .add("name", omeZarrMultiscales.name)
                .add("type", omeZarrMultiscales.type)
                .add("version", omeZarrMultiscales.version);
        if (omeZarrMultiscales.coordinateTransformations != null) {
            factory.add("coordinateTransformations", createJsonArrayFromList(omeZarrMultiscales.coordinateTransformations));
        }
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        jsonArray.add(factory.build());
        JsonElement element = gson.fromJson(jsonArray.build().toString(), JsonElement.class);
        map.put(MULTI_SCALE_KEY, element);
        return map;
    }

    private static JsonArray createJsonArrayFromList(List<ZarrAxis> list) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for(ZarrAxis zarrAxis : list) {
            String unit = zarrAxis.getUnit() == null ? "" : zarrAxis.getUnit();
            String type = zarrAxis.getType() == null ? "" : zarrAxis.getType();
            jsonArray.add(Json.createObjectBuilder()
                    .add("name", zarrAxis.getName())
                    .add("type", type)
                    .add("unit", unit));
        }
        return jsonArray.build();
    }

    private static JsonArray createJsonArrayFromList(OmeZarrMultiscales.Dataset[] datasets) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (OmeZarrMultiscales.Dataset dataset : datasets) {
            jsonArray.add(Json.createObjectBuilder()
                    .add("path", dataset.path)
                    .add("coordinateTransformations", createJsonArrayFromList(dataset.coordinateTransformations)));
        }
        return jsonArray.build();
    }

    private static JsonArray createJsonArrayFromList(OmeZarrMultiscales.CoordinateTransformations[] coordinateTransformations) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (OmeZarrMultiscales.CoordinateTransformations coordinateTransformation : coordinateTransformations) {
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            if (coordinateTransformation.type != null) {
                jsonObjectBuilder.add("type", coordinateTransformation.type);
            }
            JsonArrayBuilder scale = Json.createArrayBuilder();
            double[] scaleData = coordinateTransformation.scale;
            if (scaleData != null) {
                for (double scaleDatum : scaleData) {
                    scale.add(scaleDatum);
                }
                jsonObjectBuilder.add("scale", scale);
            }
            JsonArrayBuilder translation = Json.createArrayBuilder();
            double[] translationData = coordinateTransformation.translation;
            if (translationData != null) {
                for (double translationDatum : translationData) {
                    translation.add(translationDatum);
                }
                jsonObjectBuilder.add("translation", translation);
            }

            if (coordinateTransformation.path != null) {
                jsonObjectBuilder.add("path", coordinateTransformation.path);
            }

            jsonArray.add(jsonObjectBuilder);
        }
        return jsonArray.build();
    }
}
