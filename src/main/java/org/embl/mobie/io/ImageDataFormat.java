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
package org.embl.mobie.io;

import com.google.gson.annotations.SerializedName;

/**
 * Currently mobie-io supports the following data formats:
 * <p>
 * bdv.n5 and bdv.n5.s3
 * The data is stored in the n5 data format.
 * The bdv n5 format is used to store additional metadata about timepoints,
 * the multi-scale image pyramid and coordinateTransformations.
 * The xml is extended with custom fields that describe the s3 storage.
 * <p>
 * bdv.hdf5
 * The data is stored in the HDF5 data format, using the bdv hdf5 format
 * to represent image metadata. This format can only be read locally and does not
 * support remote access from an object store.
 * <p>
 * openOrganelle.s3
 * The data is stored in the open organelle data format, which is based on n5.
 * Currently, this data format can only be streamed from s3.
 * <p>
 * bdv.ome.zarr and bdv.ome.zarr.s3
 * The data is stored in the ome zarr file format and uses the same xml format
 * as in the bdv n5 format, but using bdv.ome.zarr as ImageLoader format.
 * The custom xml fields for bdv.ome.zarr.s3 are identical to bdv.n5.s3.
 * <p>
 * ome.zarr and ome.zarr.s3
 * The data is stored in the ome zarr file format.
 * Does not use xml with additional metadata.
 * <p>
 * <p>
 * ims
 * The data is stored in the hdf5 based Imaris file format (https://imaris.oxinst.com/support/imaris-file-format)
 * <p>
 * 's3' ending indicates that the data is taken from the remote s3 object store.
 */
public enum ImageDataFormat {
    @SerializedName("bdv.hdf5")
    BdvHDF5,
    @SerializedName("bdv.n5")
    BdvN5,
    @SerializedName("bdv.n5.s3")
    BdvN5S3,
    @SerializedName("openOrganelle.s3")
    OpenOrganelleS3,
    @SerializedName("ome.zarr")
    OmeZarr,
    @SerializedName("ome.zarr.s3")
    OmeZarrS3,
    @SerializedName("bdv.ome.zarr")
    BdvOmeZarr,
    @SerializedName("bdv.ome.zarr.s3")
    BdvOmeZarrS3,
    @SerializedName("ims")
    Imaris;

    // needed for SciJava Command UI, which does not support enums
    public static final String BDVN5 = "BdvN5";
    public static final String BDVN5S3 = "BdvN5S3";
    public static final String OPENORGANELLES3 = "OpenOrganelleS3";
    public static final String BDVOMEZARR = "BdvOmeZarr";
    public static final String BDVOMEZARRS3 = "BdvOmeZarrS3";
    public static final String OMEZARR = "OmeZarr";
    public static final String OMEZARRS3 = "OmeZarrS3";
    public static final String BDVHDF5 = "BdvHDF5";
    public static final String IMARIS = "Imaris";

    public static ImageDataFormat fromString(String string) {
        switch (string) {
            case "bdv.h5":
                return BdvHDF5;
            case "bdv.n5":
                return BdvN5;
            case "bdv.n5.s3":
                return BdvN5S3;
            case "openOrganelle":
                return OpenOrganelleS3;
            case "bdv.ome.zarr":
                return BdvOmeZarr;
            case "ome.zarr":
                return OmeZarr;
            case "bdv.ome.zarr.s3":
                return BdvOmeZarrS3;
            case "ome.zarr.s3":
                return OmeZarrS3;
            case "bdv.hdf5":
                return BdvHDF5;
            case "ims":
                return Imaris;
            default:
                throw new UnsupportedOperationException("Unknown file format: " + string);
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case BdvHDF5:
                return "bdv.h5";
            case BdvN5:
                return "bdv.n5";
            case BdvN5S3:
                return "bdv.n5.s3";
            case OpenOrganelleS3:
                return "openOrganelle.s3";
            case BdvOmeZarr:
                return "bdv.ome.zarr";
            case OmeZarr:
                return "ome.zarr";
            case BdvOmeZarrS3:
                return "bdv.ome.zarr.s3";
            case OmeZarrS3:
                return "ome.zarr.s3";
            case Imaris:
                return "ims";
            default:
                throw new UnsupportedOperationException("Unknown file format: " + this);
        }
    }

    public boolean isRemote() {
        switch (this) {
            case BdvN5S3:
            case OmeZarrS3:
            case BdvOmeZarrS3:
            case OpenOrganelleS3:
                return true;
            case BdvN5:
            case BdvOmeZarr:
            case OmeZarr:
            case BdvHDF5:
            default:
                return false;
        }
    }

    public boolean hasXml() {
        switch (this) {
            case BdvN5S3:
            case BdvOmeZarr:
            case BdvN5:
            case BdvOmeZarrS3:
                return true;
            case OmeZarr:
            case OpenOrganelleS3:
            case OmeZarrS3:
            default:
                return false;
        }
    }
}
