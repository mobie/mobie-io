package de.embl.cba.n5.ome.zarr.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.GsonAttributesParser;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.zarr.DType;
import org.janelia.saalfeldlab.n5.zarr.Filter;
import org.janelia.saalfeldlab.n5.zarr.ZarrCompressor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

public class N5ZarrImageReaderHelper extends N5FSReader {

    public N5ZarrImageReaderHelper(GsonBuilder gsonBuilder) throws IOException {
        super("", gsonBuilder);
    }
    public N5ZarrImageReaderHelper(String basePath, GsonBuilder gsonBuilder) throws IOException {
        super(basePath, gsonBuilder);
    }

    public N5ZarrImageReaderHelper(String basePath) throws IOException {
        super(basePath);
    }

    public Integer getZarrFormatFromMeta(final HashMap<String, JsonElement> meta) throws IOException {
        return GsonAttributesParser.parseAttribute(
                meta,
                "zarr_format",
                Integer.class,
                gson);
    }

    public Version getVersionFromAttributes(HashMap<String, JsonElement> attributes) throws IOException {
        final Integer zarr_format = GsonAttributesParser.parseAttribute(
                attributes,
                "zarr_format",
                Integer.class,
                gson);
        if (zarr_format != null)
            return new Version(zarr_format, 0, 0);
        else
            return null;
    }

    public ZArrayAttributes getN5DatasetAttributes(String pathName, @NotNull HashMap<String, JsonElement> attributes) throws IOException {
            return new ZArrayAttributes(
                    attributes.get("zarr_format").getAsInt(),
                    gson.fromJson(attributes.get("shape"), long[].class),
                    gson.fromJson(attributes.get("chunks"), int[].class),
                    gson.fromJson(attributes.get("dtype"), DType.class),
                    gson.fromJson(attributes.get("compressor"), ZarrCompressor.class),
                    attributes.get("fill_value").getAsString(),
                    attributes.get("order").getAsCharacter(),
                    gson.fromJson(attributes.get("filters"), TypeToken.getParameterized(Collection.class, Filter.class).getType()));

    }

    public void putAttributes(HashMap<String, JsonElement> attributes, DatasetAttributes datasetAttributes) {
        attributes.put("dimensions", gson.toJsonTree(datasetAttributes.getDimensions()));
        attributes.put("blockSize", gson.toJsonTree(datasetAttributes.getBlockSize()));
        attributes.put("dataType", gson.toJsonTree(datasetAttributes.getDataType()));
        attributes.put("compression", gson.toJsonTree(datasetAttributes.getCompression()));
    }


    @Override
    public HashMap<String, JsonElement> getAttributes(String pathName) throws IOException {
        return null;
    }

    @Override
    public DataBlock<?> readBlock(String pathName, DatasetAttributes datasetAttributes, long[] gridPosition) throws IOException {
        return null;
    }

    @Override
    public boolean exists(String pathName) {
        return false;
    }

    @Override
    public String[] list(String pathName) throws IOException {
        return new String[0];
    }
}
