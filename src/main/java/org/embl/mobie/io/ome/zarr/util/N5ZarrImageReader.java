/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.io.ome.zarr.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.janelia.saalfeldlab.n5.BlockReader;
import org.janelia.saalfeldlab.n5.ByteArrayDataBlock;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.zarr.DType;
import org.janelia.saalfeldlab.n5.zarr.ZarrCompressor;
import org.janelia.saalfeldlab.n5.zarr.ZarrDatasetAttributes;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public interface N5ZarrImageReader extends N5Reader {
    String DEFAULT_SEPARATOR = ".";
    String zarrayFile = ".zarray";
    String zattrsFile = ".zattrs";
    String zgroupFile = ".zgroup";
    List<ZarrAxis> zarrAxes = new ArrayList<>();


    static GsonBuilder initGsonBuilder(final GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(DType.class, new DType.JsonAdapter());
        gsonBuilder.registerTypeAdapter(ZarrCompressor.class, ZarrCompressor.jsonAdapter);
        gsonBuilder.registerTypeAdapter(ZarrAxes.class, new ZarrAxesAdapter());
        gsonBuilder.registerTypeAdapter(N5Reader.Version.class, new VersionAdapter());
        gsonBuilder.registerTypeAdapter(OmeZarrMultiscales.class, new OmeZarrMultiscalesAdapter());
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

    default void readDimensions(HashMap<String, JsonElement> attributes) throws IllegalArgumentException {
        JsonElement multiscales = attributes.get("multiscales");
        if (multiscales == null) {
            return;
        }
        String version = multiscales.getAsJsonArray().get(0).getAsJsonObject().get("version").getAsString();
        if (version.equals("0.4")) {
            JsonArray axes = multiscales.getAsJsonArray().get(0).getAsJsonObject().get("axes").getAsJsonArray();
            int index = 0;
            List<ZarrAxis> zarrAxisArrayList = new ArrayList<>();
            for (JsonElement axis : axes) {
                String name = axis.getAsJsonObject().get("name").getAsString();
                String type = axis.getAsJsonObject().get("type").getAsString();
                if (name.isEmpty() || type.isEmpty() || !AxesTypes.contains(type)) {
                    throw new IllegalArgumentException("Unsupported multiscales axes: " + name + ", " + type);
                }
                ZarrAxis zarrAxis;
                if (axis.getAsJsonObject().get("unit") != null && axis.getAsJsonObject().get("unit").isJsonPrimitive()) {
                    String unit = axis.getAsJsonObject().get("unit").getAsString();
                    zarrAxis = new ZarrAxis(index, name, type, unit);
                } else {
                    zarrAxis = new ZarrAxis(index, name, type);
                }
                index++;
                zarrAxisArrayList.add(zarrAxis);
            }
            setAxes(zarrAxisArrayList);
            setAxes(ZarrAxis.convertToJson(zarrAxisArrayList));
        } else {
            JsonElement axes = multiscales.getAsJsonArray().get(0).getAsJsonObject().get("axes");
            setAxes(axes);
        }
    }

    void setAxes(JsonElement axesJson);

    void setAxes(List<ZarrAxis> axes);

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
