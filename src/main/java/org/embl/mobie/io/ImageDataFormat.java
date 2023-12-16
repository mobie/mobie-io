/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2023 EMBL
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

import static org.embl.mobie.io.ImageDataFormatNames.*;

/**
 * Currently mobie-io supports the following data formats:
 * <p>
 * bdv
 * This includes all file formats that come with a BDV XML.
 * Below some variants are mentioned specifically as bdv.*
 * All file formats starting with bdv must include the XML.
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
    @SerializedName(TOML)
    Toml,
    @SerializedName(TIFF)
    Tiff,
    @SerializedName(IMAGEJ)
    ImageJ,
    @SerializedName(BIOFORMATS)
    BioFormats,
    @SerializedName(BIOFORMATSS3)
    BioFormatsS3,
    @SerializedName(BDV)
    Bdv,
    @SerializedName(BDVHDF5)
    BdvHDF5,
    @SerializedName(BDVN5)
    BdvN5,
    @SerializedName(BDVN5S3)
    BdvN5S3,
    @SerializedName(OPENORGANELLES3)
    OpenOrganelleS3,
    @SerializedName(OMEZARR)
    OmeZarr,
    @SerializedName(OMEZARRS3)
    OmeZarrS3,
    @SerializedName(BDVOMEZARR)
    BdvOmeZarr,
    @SerializedName(BDVOMEZARRS3)
    BdvOmeZarrS3,
    @SerializedName(IMARIS)
    Imaris,
    @SerializedName(SPIMDATA)
    SpimData,
    @SerializedName(ILASTIKHDF5)
    IlastikHDF5;

    public static ImageDataFormat fromString(String string) {
        switch (string) {
            case TOML:
                return Toml;
            case IMAGEJ:
                return ImageJ;
            case BIOFORMATS:
                return BioFormats;
            case BIOFORMATSS3:
                return BioFormatsS3;
            case BDV:
                return Bdv;
            case BDVHDF5:
                return BdvHDF5;
            case BDVN5:
                return BdvN5;
            case BDVN5S3:
                return BdvN5S3;
            case OPENORGANELLES3:
                return OpenOrganelleS3;
            case BDVOMEZARR:
                return BdvOmeZarr;
            case OMEZARR:
                return OmeZarr;
            case BDVOMEZARRS3:
                return BdvOmeZarrS3;
            case OMEZARRS3:
                return OmeZarrS3;
            case IMARIS:
                return Imaris;
            case SPIMDATA:
                return SpimData;
            case ILASTIKHDF5:
                return IlastikHDF5;
            default:
                throw new UnsupportedOperationException("Unknown file format: " + string);
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case Toml:
                return TOML;
            case Tiff:
                return TIFF;
            case ImageJ:
                return IMAGEJ;
            case BioFormats:
                return BIOFORMATS;
            case BioFormatsS3:
                return BIOFORMATSS3;
            case Bdv:
                return BDV;
            case BdvHDF5:
                return BDVHDF5;
            case BdvN5:
                return BDVN5;
            case BdvN5S3:
                return BDVN5S3;
            case OpenOrganelleS3:
                return OPENORGANELLES3;
            case BdvOmeZarr:
                return BDVOMEZARRS3;
            case OmeZarr:
                return OMEZARR;
            case BdvOmeZarrS3:
                return BDVOMEZARR;
            case OmeZarrS3:
                return OMEZARRS3;
            case Imaris:
                return IMARIS;
            case SpimData:
                return SPIMDATA;
            case IlastikHDF5:
                return ILASTIKHDF5;
            default:
                throw new UnsupportedOperationException("Unknown file format: " + this);
        }
    }

    public static ImageDataFormat fromPath(String path)
    {
        final String lowerCase = path.toLowerCase();
        if(lowerCase.contains( ".zarr" ))
            return ImageDataFormat.OmeZarr;
        else if (lowerCase.endsWith( ".xml" ))
            return ImageDataFormat.Bdv; // TODO: https://github.com/mobie/mobie-io/issues/131
        else if (lowerCase.endsWith( ".ome.tif" ) || lowerCase.endsWith( ".ome.tiff" ) )
            return ImageDataFormat.BioFormats;
        else if (lowerCase.endsWith( ".tif" ) || lowerCase.endsWith( ".tiff" ))
            return ImageDataFormat.Tiff;
        else if (lowerCase.endsWith( ".h5" ))
            return ImageDataFormat.IlastikHDF5;
        else if (lowerCase.endsWith( ".toml" ))
            return ImageDataFormat.Toml;
        else
            return ImageDataFormat.BioFormats;
    }

    public boolean inMemory()
    {
        switch (this) {
            case SpimData:
                return true;
            default:
                return false;
        }
    }

    public boolean isRemote() {
        switch (this) {
            case BdvN5S3:
            case OmeZarrS3:
            case BdvOmeZarrS3:
            case OpenOrganelleS3:
            case BioFormatsS3:
                return true;
            case BdvN5:
            case BdvOmeZarr:
            case OmeZarr:
            case BdvHDF5:
            case ImageJ:
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
