package de.embl.cba.n5.ome.zarr.util;

import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.zarr.DType;

public class ZarrDatasetAttributes extends org.janelia.saalfeldlab.n5.zarr.ZarrDatasetAttributes {
    private final transient String fillValue;

    public ZarrDatasetAttributes(
            final long[] dimensions,
            final int[] blockSize,
            final DType dType,
            final Compression compression,
            final boolean isRowMajor,
            final String fill_value) {
        super(dimensions, blockSize, dType, compression, isRowMajor, fill_value);
        this.fillValue = fill_value;
    }

    public String getFillValue() {
        return fillValue;
    }
}
