package org.embl.mobie.io.ome.zarr.util;

import java.util.Collection;
import java.util.HashMap;

import org.janelia.saalfeldlab.n5.zarr.DType;
import org.janelia.saalfeldlab.n5.zarr.Filter;
import org.janelia.saalfeldlab.n5.zarr.ZarrCompressor;

import com.google.gson.annotations.SerializedName;

public class OmeZArrayAttributes extends ZArrayAttributes {
    protected static final String dimensionSeparatorKey = "dimension_separator";

    @SerializedName("dimension_separator")
    private final String dimensionSeparator;

    public OmeZArrayAttributes(int zarr_format, long[] shape, int[] chunks, DType dtype, ZarrCompressor compressor,
                               String fill_value, char order, Collection<Filter> filters, String dimensionSeparator) {
        super(zarr_format, shape, chunks, dtype, compressor, fill_value, order, filters);
        this.dimensionSeparator = dimensionSeparator;
    }

    public String getDimensionSeparator() {
        return dimensionSeparator;
    }

    public HashMap<String, Object> asMap() {

        final HashMap<String, Object> map = super.asMap();
        map.put(dimensionSeparatorKey, dimensionSeparator);

        return map;
    }
}
