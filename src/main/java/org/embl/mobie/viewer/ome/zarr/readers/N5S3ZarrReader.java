/**
 * Copyright (c) 2019, Stephan Saalfeld
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.embl.mobie.viewer.ome.zarr.readers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.embl.mobie.viewer.ome.zarr.util.*;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.GsonAttributesParser;
import org.janelia.saalfeldlab.n5.s3.N5AmazonS3Reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


/**
 * Attempt at a diamond inheritance solution for S3+Zarr.
 */
public class N5S3ZarrReader extends N5AmazonS3Reader implements N5ZarrImageReader {

    final protected boolean mapN5DatasetAttributes;
    private final String serviceEndpoint;
    private final N5ZarrImageReaderHelper n5ZarrImageReaderHelper;
    protected String dimensionSeparator;
    private ZarrAxes zarrAxes;

    public N5S3ZarrReader(AmazonS3 s3, String serviceEndpoint, String bucketName, String containerPath, String dimensionSeparator) throws IOException {
        super(s3, bucketName, containerPath, N5ZarrImageReader.initGsonBuilder(new GsonBuilder()));
        this.serviceEndpoint = serviceEndpoint; // for debugging
        this.dimensionSeparator = dimensionSeparator;
        mapN5DatasetAttributes = true;
        this.n5ZarrImageReaderHelper = new N5ZarrImageReaderHelper(N5ZarrImageReader.initGsonBuilder(new GsonBuilder()));
    }

    public ZarrAxes getAxes() {
        return this.zarrAxes;
    }

    @Override
    public void setAxes(JsonElement axesJson) {
        if (axesJson != null) {
            this.zarrAxes = ZarrAxes.decode(axesJson.toString());
        } else {
            this.zarrAxes = ZarrAxes.NOT_SPECIFIED;
        }
    }

    public void setDimensionSeparator(String dimensionSeparator) {
        this.dimensionSeparator = dimensionSeparator;
    }

    public AmazonS3 getS3() {
        return s3;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    /**
     * Helper to encapsulate building the object key for a file like
     * .zarray or .zgroup within any given path.
     *
     * @param pathName
     * @param file     One of .zarray, .zgroup or .zattrs
     * @return
     */
    private String objectFile(final String pathName, String file) {
        StringBuilder sb = new StringBuilder();
        sb.append(containerPath);
        String cleaned = removeLeadingSlash(pathName);
        if (!cleaned.isEmpty()) {
            sb.append('/');
            sb.append(cleaned);
        }
        sb.append('/');
        sb.append(file);
        return sb.toString();
    }

    // remove getBasePath

    @Override
    public Version getVersion() throws IOException {
        HashMap<String, JsonElement> meta;
        meta = readJson(objectFile("", zgroupFile));
        if (meta == null) {
            meta = readJson(objectFile("", zarrayFile));
        }

        if (meta != null) {

            final Integer zarr_format = GsonAttributesParser.parseAttribute(
                    meta,
                    "zarr_format",
                    Integer.class,
                    gson);

            if (zarr_format != null)
                return new Version(zarr_format, 0, 0);
        }

        return VERSION;
    }

    public boolean groupExists(final String pathName) {
        return exists(objectFile(pathName, zgroupFile));
    }

    public ZArrayAttributes getZArrayAttributes(final String pathName) throws IOException {
        final String path = objectFile(pathName, zarrayFile);
        HashMap<String, JsonElement> attributes = readJson(path);

        if (attributes == null) {
            System.out.println(path + " does not exist.");
            attributes = new HashMap<>();
        }

        JsonElement dimSep = attributes.get("dimension_separator");
        this.dimensionSeparator = dimSep == null ? DEFAULT_SEPARATOR : dimSep.getAsString();
        return n5ZarrImageReaderHelper.getN5DatasetAttributes(attributes);
    }

    @Override
    public DatasetAttributes getDatasetAttributes(final String pathName) throws IOException {
        final ZArrayAttributes zArrayAttributes = getZArrayAttributes(pathName);
        return zArrayAttributes == null ? null : zArrayAttributes.getDatasetAttributes();
    }

    @Override
    public boolean datasetExists(final String pathName) throws IOException {
        final String path = objectFile(pathName, zarrayFile);
        return readJson(path) != null;
    }

    /**
     * CHANGE: rename to not overwrite the AWS list objects version
     *
     * @returns false if the group or dataset does not exist but also if the
     * attempt to access
     */
    // @Override
    public boolean zarrExists(final String pathName) {
        try {
            return groupExists(pathName) || datasetExists(pathName);
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * If {@link #mapN5DatasetAttributes} is set, dataset attributes will
     * override attributes with the same key.
     */
    @Override
    public HashMap<String, JsonElement> getAttributes(final String pathName) throws IOException {
        final String path = objectFile(pathName, zattrsFile);
        HashMap<String, JsonElement> attributes = readJson(path);

        if (attributes == null) {
            attributes = new HashMap<>();
        }
        getDimensions(attributes);
        if (mapN5DatasetAttributes && datasetExists(pathName)) {
            final DatasetAttributes datasetAttributes = getZArrayAttributes(pathName).getDatasetAttributes();
            n5ZarrImageReaderHelper.putAttributes(attributes, datasetAttributes);
        }
        return attributes;
    }

    @Override
    public DataBlock<?> readBlock(
            final String pathName,
            final DatasetAttributes datasetAttributes,
            final long... gridPosition) throws IOException {
        final ZarrDatasetAttributes zarrDatasetAttributes;
        if (datasetAttributes instanceof ZarrDatasetAttributes)
            zarrDatasetAttributes = (ZarrDatasetAttributes) datasetAttributes;
        else
            zarrDatasetAttributes = getZArrayAttributes(pathName).getDatasetAttributes();

        final String dataBlockKey =
                objectFile(pathName,
                        getZarrDataBlockString(
                                gridPosition,
                                dimensionSeparator,
                                zarrDatasetAttributes.isRowMajor()));

        // Currently exists() appends "/"
        //		if (!exists(dataBlockKey))
        //			return null;

        try {
            try (final InputStream in = this.readS3Object(dataBlockKey)) {
                return readBlock(in, zarrDatasetAttributes, gridPosition);
            }
        } catch (AmazonS3Exception ase) {
            if ("NoSuchKey".equals(ase.getErrorCode())) {
                return null;
            }
            throw ase;
        }
    }

    /**
     * Copied from getAttributes but doesn't change the objectPath in anyway.
     * CHANGES: returns null rather than empty hash map
     *
     * @param objectPath
     * @return null if the object does not exist, otherwise the loaded attributes.
     * @throws IOException
     */
    public HashMap<String, JsonElement> readJson(String objectPath) throws IOException {
        if (!this.s3.doesObjectExist(this.bucketName, objectPath)) {
            return null;
        } else {
            InputStream in = this.readS3Object(objectPath);
            Throwable var4 = null;

            HashMap<String, JsonElement> var5;
            try {
                var5 = GsonAttributesParser.readAttributes(new InputStreamReader(in), this.gson);
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (in != null) {
                    if (var4 != null) {
                        try {
                            in.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        in.close();
                    }
                }

            }

            return var5;
        }
    }

}