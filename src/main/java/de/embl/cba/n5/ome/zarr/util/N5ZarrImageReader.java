package de.embl.cba.n5.ome.zarr.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.janelia.saalfeldlab.n5.BlockReader;
import org.janelia.saalfeldlab.n5.ByteArrayDataBlock;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.zarr.DType;
import org.janelia.saalfeldlab.n5.zarr.ZarrCompressor;
import org.janelia.saalfeldlab.n5.zarr.ZarrDatasetAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

public interface N5ZarrImageReader extends N5Reader {
    String DEFAULT_SEPARATOR = ".";
    String zarrayFile = ".zarray";
    String zattrsFile = ".zattrs";
    String zgroupFile = ".zgroup";


    static GsonBuilder initGsonBuilder(final GsonBuilder gsonBuilder) {

        gsonBuilder.registerTypeAdapter(DType.class, new DType.JsonAdapter());
        gsonBuilder.registerTypeAdapter(ZarrCompressor.class, ZarrCompressor.jsonAdapter);
        gsonBuilder.serializeNulls();
        gsonBuilder.registerTypeAdapter(ZarrAxes.class, new ZarrAxesAdapter());
        gsonBuilder.registerTypeAdapter(N5Reader.Version.class, new VersionAdapter());
        gsonBuilder.setPrettyPrinting();
        return gsonBuilder;
    }

    default Version getVersion() throws IOException {
        return VERSION;
    }

    default String getDimensionSeparator(HashMap<String, JsonElement> attributes) {
        JsonElement dimSep = attributes.get("dimension_separator");
        return dimSep == null ? DEFAULT_SEPARATOR : dimSep.getAsString();
    }

//////////////////////////////////////////////////////TODO:
    default void getDimensions(HashMap<String, JsonElement> attributes) {
        JsonElement multiscales = attributes.get("multiscales");
        if (multiscales != null) {
            JsonElement axes = multiscales.getAsJsonArray().get(0).getAsJsonObject().get("axes");
            setAxes(axes);
        }
    }

    void setAxes(JsonElement axesJson);

    ZArrayAttributes getZArrayAttributes(final String pathName) throws IOException;

    boolean datasetExists(final String pathName) throws IOException;

    boolean groupExists(final String pathName);

    /**
     * CHANGE: return String rather than Path, fixed javadoc
     * Constructs the path for a data block in a dataset at a given grid position.
     * <p>
     * The returned path is
     * <pre>
     * $datasetPathName/$gridPosition[n]$dimensionSeparator$gridPosition[n-1]$dimensionSeparator[...]$dimensionSeparator$gridPosition[0]
     * </pre>
     * <p>
     * This is the file into which the data block will be stored.
     *
     * @param gridPosition
     * @param dimensionSeparator
     * @return
     */
    default String getZarrDataBlockString(
            final long[] gridPosition,
            final String dimensionSeparator,
            final boolean isRowMajor) {
        final StringBuilder pathStringBuilder = new StringBuilder();
        if (isRowMajor) {
            pathStringBuilder.append(gridPosition[gridPosition.length - 1]);
            for (int i = gridPosition.length - 2; i >= 0; --i) {
                pathStringBuilder.append(dimensionSeparator);
                pathStringBuilder.append(gridPosition[i]);
            }
        } else {
            pathStringBuilder.append(gridPosition[0]);
            for (int i = 1; i < gridPosition.length; ++i) {
                pathStringBuilder.append(dimensionSeparator);
                pathStringBuilder.append(gridPosition[i]);
            }
        }

        return pathStringBuilder.toString();
    }

    /**
     * Reads a {@link DataBlock} from an {@link InputStream}.
     *
     * @param in
     * @param datasetAttributes
     * @param gridPosition
     * @return
     * @throws IOException
     */
    @SuppressWarnings("incomplete-switch")
    default DataBlock<?> readBlock(
            final InputStream in,
            final ZarrDatasetAttributes datasetAttributes,
            final long... gridPosition) throws IOException {
        final int[] blockSize = datasetAttributes.getBlockSize();
        final DType dType = datasetAttributes.getDType();

        final ByteArrayDataBlock byteBlock = dType.createByteBlock(blockSize, gridPosition);

        final BlockReader reader = datasetAttributes.getCompression().getReader();
        reader.read(byteBlock, in);

        switch (dType.getDataType()) {
            case UINT8:
            case INT8:
                return byteBlock;
        }

        /* else translate into target type */
        final DataBlock<?> dataBlock = dType.createDataBlock(blockSize, gridPosition);
        final ByteBuffer byteBuffer = byteBlock.toByteBuffer();
        byteBuffer.order(dType.getOrder());
        dataBlock.readData(byteBuffer);

        return dataBlock;
    }

}
