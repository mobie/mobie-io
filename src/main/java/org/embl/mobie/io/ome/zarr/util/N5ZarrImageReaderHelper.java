package org.embl.mobie.io.ome.zarr.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.zarr.DType;
import org.janelia.saalfeldlab.n5.zarr.Filter;
import org.janelia.saalfeldlab.n5.zarr.ZarrCompressor;
import org.jetbrains.annotations.NotNull;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class N5ZarrImageReaderHelper extends N5FSReader {

    public N5ZarrImageReaderHelper(GsonBuilder gsonBuilder) throws IOException {
        super("", gsonBuilder);
    }

    public N5ZarrImageReaderHelper(String basePath, GsonBuilder gsonBuilder) throws IOException {
        super(basePath, gsonBuilder);
    }

    public ZArrayAttributes getN5DatasetAttributes(@NotNull HashMap<String, JsonElement> attributes) throws IOException {
        if (attributes.isEmpty()) {
            throw new IOException("Empty ZArray attributes");
        }
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
    public HashMap<String, JsonElement> getAttributes(String pathName) {
        return null;
    }

    @Override
    public DataBlock<?> readBlock(String pathName, DatasetAttributes datasetAttributes, long[] gridPosition) {
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
