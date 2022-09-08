package org.embl.mobie.io.ome.zarr.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.RawCompression;
import org.janelia.saalfeldlab.n5.zarr.DType;
import org.janelia.saalfeldlab.n5.zarr.Filter;
import org.janelia.saalfeldlab.n5.zarr.Utils;
import org.janelia.saalfeldlab.n5.zarr.ZarrCompressor;

public class ZArrayAttributes {

    protected static final String zarrFormatKey = "zarr_format";
    protected static final String shapeKey = "shape";
    protected static final String chunksKey = "chunks";
    protected static final String dTypeKey = "dtype";
    protected static final String compressorKey = "compressor";
    protected static final String fillValueKey = "fill_value";
    protected static final String orderKey = "order";
    protected static final String filtersKey = "filters";

    private final int zarr_format;
    private final long[] shape;
    private final int[] chunks;
    private final DType dtype;
    private final ZarrCompressor compressor;
    private final String fill_value;
    private final char order;
    private final List<Filter> filters = new ArrayList<>();

    public ZArrayAttributes(
        final int zarr_format,
        final long[] shape,
        final int[] chunks,
        final DType dtype,
        final ZarrCompressor compressor,
        final String fill_value,
        final char order,
        final Collection<Filter> filters) {

        this.zarr_format = zarr_format;
        this.shape = shape;
        this.chunks = chunks;
        this.dtype = dtype;
        this.compressor = compressor == null ? new ZarrCompressor.Raw() : compressor;
        this.fill_value = fill_value;
        this.order = order;
        if (filters != null)
            this.filters.addAll(filters);
    }

    public ZarrDatasetAttributes getDatasetAttributes() {
        final boolean isRowMajor = order == 'C';
        final long[] dimensions = shape.clone();
        final int[] blockSize = chunks.clone();

        if (isRowMajor) {
            Utils.reorder(dimensions);
            Utils.reorder(blockSize);
        }

        Compression compression = compressor != null ? compressor.getCompression() : new ZarrCompressor.Raw().getCompression();

        return new ZarrDatasetAttributes(
            dimensions,
            blockSize,
            dtype,
            compression,
            isRowMajor,
            fill_value);
    }

    public long[] getShape() {
        return shape;
    }

    public int getNumDimensions() {
        return shape.length;
    }

    public int[] getChunks() {
        return chunks;
    }

    public ZarrCompressor getCompressor() {
        return compressor;
    }

    public DType getDType() {
        return dtype;
    }

    public int getZarrFormat() {
        return zarr_format;
    }

    public char getOrder() {
        return order;
    }

    public String getFillValue() {
        return fill_value;
    }

    public HashMap<String, Object> asMap() {

        final HashMap<String, Object> map = new HashMap<>();

        map.put(zarrFormatKey, zarr_format);
        map.put(shapeKey, shape);
        map.put(chunksKey, chunks);
        map.put(dTypeKey, dtype.toString());
        map.put(compressorKey, compressor instanceof RawCompression ? null : compressor);
        map.put(fillValueKey, fill_value);
        map.put(orderKey, order);
        map.put(filtersKey, filters);

        return map;
    }

    public Collection<Filter> getFilters() {
        return filters;
    }


}