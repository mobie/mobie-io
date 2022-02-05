package org.embl.mobie.io.ome.zarr.loaders.xml;

import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.ImgLoaderIo;
import mpicbg.spim.data.generic.sequence.XmlIoBasicImgLoader;
import org.embl.mobie.io.ome.zarr.loaders.N5S3OMEZarrImageLoader;
import org.jdom2.Element;

import java.io.File;
import java.io.IOException;

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
		final String bucketName = XmlHelpers.getText( elem, BUCKET_NAME);
		final String key = XmlHelpers.getText(elem, KEY);
		try
		{
			return new N5S3OMEZarrImageLoader(serviceEndpoint, signingRegion, bucketName, key, "/", sequenceDescription);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}