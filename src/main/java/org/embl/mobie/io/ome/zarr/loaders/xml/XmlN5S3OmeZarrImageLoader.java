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
package org.embl.mobie.io.ome.zarr.loaders.xml;

import java.io.File;
import java.io.IOException;

import org.embl.mobie.io.ome.zarr.loaders.N5S3OMEZarrImageLoader;
import org.jdom2.Element;

import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.ImgLoaderIo;
import mpicbg.spim.data.generic.sequence.XmlIoBasicImgLoader;

import static mpicbg.spim.data.XmlKeys.IMGLOADER_FORMAT_ATTRIBUTE_NAME;

@ImgLoaderIo(format = "bdv.ome.zarr.s3", type = N5S3OMEZarrImageLoader.class)
public class XmlN5S3OmeZarrImageLoader implements XmlIoBasicImgLoader<N5S3OMEZarrImageLoader> {
    public static final String SERVICE_ENDPOINT = "ServiceEndpoint";
    public static final String SIGNING_REGION = "SigningRegion";
    public static final String BUCKET_NAME = "BucketName";
    public static final String KEY = "Key";

    @Override
    public Element toXml(final N5S3OMEZarrImageLoader imgLoader, final File basePath) {
        final Element elem = new Element("ImageLoader");
        elem.setAttribute(IMGLOADER_FORMAT_ATTRIBUTE_NAME, "bdv.ome.zarr.s3");
        elem.setAttribute("version", "0.2");
        elem.addContent(XmlHelpers.textElement(SERVICE_ENDPOINT, imgLoader.getServiceEndpoint()));
        elem.addContent(XmlHelpers.textElement(SIGNING_REGION, imgLoader.getSigningRegion()));
        elem.addContent(XmlHelpers.textElement(BUCKET_NAME, imgLoader.getBucketName()));
        elem.addContent(XmlHelpers.textElement(KEY, imgLoader.getKey()));
        return elem;
    }

    public Element toXml(String serviceEndpoint, String signingRegion, String bucketName, String key) {
        final Element elem = new Element("ImageLoader");
        elem.setAttribute(IMGLOADER_FORMAT_ATTRIBUTE_NAME, "bdv.ome.zarr.s3");
        elem.addContent(new Element(KEY).addContent(key));
        elem.addContent(new Element(SIGNING_REGION).addContent(signingRegion));
        elem.addContent(new Element(SERVICE_ENDPOINT).addContent(serviceEndpoint));
        elem.addContent(new Element(BUCKET_NAME).addContent(bucketName));

        return elem;
    }

    @Override
    public N5S3OMEZarrImageLoader fromXml(Element elem, File basePath, AbstractSequenceDescription<?, ?, ?> sequenceDescription) {
        final String serviceEndpoint = XmlHelpers.getText(elem, SERVICE_ENDPOINT);
        final String signingRegion = XmlHelpers.getText(elem, SIGNING_REGION);
        final String bucketName = XmlHelpers.getText(elem, BUCKET_NAME);
        final String key = XmlHelpers.getText(elem, KEY);
        try {
            return new N5S3OMEZarrImageLoader(serviceEndpoint, signingRegion, bucketName, key, "/", sequenceDescription);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
