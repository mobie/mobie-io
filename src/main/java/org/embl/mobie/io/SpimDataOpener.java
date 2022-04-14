package org.embl.mobie.io;

import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import bdv.util.volatiles.SharedQueue;
import ij.IJ;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import org.embl.mobie.io.n5.openers.N5Opener;
import org.embl.mobie.io.n5.openers.N5S3Opener;
import org.embl.mobie.io.ome.zarr.loaders.N5S3OMEZarrImageLoader;
import org.embl.mobie.io.ome.zarr.loaders.xml.XmlN5OmeZarrImageLoader;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrOpener;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.embl.mobie.io.openorganelle.OpenOrganelleS3Opener;
import org.embl.mobie.io.util.CustomXmlIoSpimData;
import org.embl.mobie.io.util.FileAndUrlUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import static mpicbg.spim.data.XmlKeys.IMGLOADER_TAG;
import static mpicbg.spim.data.XmlKeys.SEQUENCEDESCRIPTION_TAG;

public class SpimDataOpener {

    public static final String ERROR_WHILE_TRYING_TO_READ_SPIM_DATA = "Error while trying to read spimData";

    public SpimDataOpener() {
    }

    public AbstractSpimData openSpimData(String imagePath, ImageDataFormat imageDataFormat) throws UnsupportedOperationException, SpimDataException {
        switch (imageDataFormat) {
            case Imaris:
                return openImaris(imagePath);
            case BdvHDF5:
            case BdvN5:
            case BdvN5S3:
                return openBdvXml(imagePath);
            case OmeZarr:
                return openOmeZarr(imagePath);
            case OmeZarrS3:
                return openOmeZarrS3(imagePath);
            case BdvOmeZarr:
                return openBdvOmeZarr(imagePath, null);
            case BdvOmeZarrS3:
                return openBdvOmeZarrS3(imagePath, null);
            case OpenOrganelleS3:
                return openOpenOrganelleS3(imagePath);
            default:
                throw new UnsupportedOperationException("Opening of " + imageDataFormat + " is not supported.");
        }
    }

    public AbstractSpimData openSpimData(String imagePath, ImageDataFormat imageDataFormat, SharedQueue sharedQueue) throws UnsupportedOperationException, SpimDataException {
        switch (imageDataFormat) {
            case BdvN5:
                return openBdvN5(imagePath, sharedQueue);
            case BdvN5S3:
                return openBdvN5S3(imagePath, sharedQueue);
            case OmeZarr:
                return openOmeZarr(imagePath, sharedQueue);
            case OmeZarrS3:
                return openOmeZarrS3(imagePath, sharedQueue);
            case BdvOmeZarr:
                return openBdvOmeZarr(imagePath, sharedQueue);
            case BdvOmeZarrS3:
                return openBdvOmeZarrS3(imagePath, sharedQueue);
            default:
                System.out.println("Shared queues for " + imageDataFormat + " are not yet supported; opening with own queue.");
                return openSpimData(imagePath, imageDataFormat);
        }
    }

    @NotNull
    private SpimDataMinimal openImaris(String imagePath) throws RuntimeException {
        try {
            return Imaris.openIms(imagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SpimData openBdvXml(String path) throws SpimDataException {
        try {
            InputStream stream = FileAndUrlUtils.getInputStream(path);
            return new CustomXmlIoSpimData().loadFromStream(stream, path);
        } catch (SpimDataException | IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openBdvN5(String path, SharedQueue queue) throws SpimDataException {
        try {
            return N5Opener.openFile(path, queue);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openBdvN5S3(String path, SharedQueue queue) throws SpimDataException {
        try {
            return N5S3Opener.readURL(path, queue);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openOmeZarr(String path) throws SpimDataException {
        try {
            return OMEZarrOpener.openFile(path);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openOmeZarr(String path, SharedQueue sharedQueue) throws SpimDataException {
        try {
            return OMEZarrOpener.openFile(path, sharedQueue);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openOmeZarrS3(String path) throws SpimDataException {
        try {
            return OMEZarrS3Opener.readURL(path);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openOmeZarrS3(String path, SharedQueue sharedQueue) throws SpimDataException {
        try {
            return OMEZarrS3Opener.readURL(path, sharedQueue);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openOpenOrganelleS3(String path) throws SpimDataException {
        try {
            return OpenOrganelleS3Opener.readURL(path);
        } catch (IOException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openBdvOmeZarrS3(String path, SharedQueue queue) throws SpimDataException {
        try {
            N5S3OMEZarrImageLoader imageLoader = createN5S3OmeZarrImageLoader(path, queue);
            SpimData spimData = openBdvXml(path);
            if (spimData != null) {
                spimData.getSequenceDescription().setImgLoader(imageLoader);
                return spimData;
            } else {
                throw new SpimDataException("Error while trying to read spimData. SpimData is null");
            }
        } catch (IOException | JDOMException e) {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA + e.getMessage());
        }
    }

    private SpimData openBdvOmeZarr(String path, @Nullable SharedQueue sharedQueue) throws SpimDataException {
        SpimData spimData = openBdvXml(path);
        SpimData spimDataWithImageLoader = getSpimDataWithImageLoader(path, sharedQueue);
        if (spimData != null && spimDataWithImageLoader != null) {
            spimData.getSequenceDescription().setImgLoader(spimDataWithImageLoader.getSequenceDescription().getImgLoader());
            spimData.getSequenceDescription().getAllChannels().putAll(spimDataWithImageLoader.getSequenceDescription().getAllChannels());
            return spimData;
        } else {
            throw new SpimDataException(ERROR_WHILE_TRYING_TO_READ_SPIM_DATA);
        }
    }

    @NotNull
    private N5S3OMEZarrImageLoader createN5S3OmeZarrImageLoader(String path, @Nullable SharedQueue queue) throws IOException, JDOMException {
        final SAXBuilder sax = new SAXBuilder();
        InputStream stream = FileAndUrlUtils.getInputStream(path);
        final Document doc = sax.build(stream);
        final Element imgLoaderElem = doc.getRootElement().getChild(SEQUENCEDESCRIPTION_TAG).getChild(IMGLOADER_TAG);
        String bucketAndObject = imgLoaderElem.getChild("BucketName").getText() + "/" + imgLoaderElem.getChild("Key").getText();
        final String[] split = bucketAndObject.split("/");
        String bucket = split[0];
        String object = Arrays.stream(split).skip(1).collect(Collectors.joining("/"));
        if (queue == null) {
            return new N5S3OMEZarrImageLoader(imgLoaderElem.getChild("ServiceEndpoint").getText(), imgLoaderElem.getChild("SigningRegion").getText(), bucket, object, ".");
        } else {
            return new N5S3OMEZarrImageLoader(imgLoaderElem.getChild("ServiceEndpoint").getText(), imgLoaderElem.getChild("SigningRegion").getText(), bucket, object, ".", queue);
        }
    }

    private SpimData getSpimDataWithImageLoader(String path, @Nullable SharedQueue sharedQueue) {
        try {
            final SAXBuilder sax = new SAXBuilder();
            InputStream stream = FileAndUrlUtils.getInputStream(path);
            final Document doc = sax.build(stream);
            final Element imgLoaderElem = doc.getRootElement().getChild(SEQUENCEDESCRIPTION_TAG).getChild(IMGLOADER_TAG);
            String imagesFile = XmlN5OmeZarrImageLoader.getDatasetsPathFromXml(imgLoaderElem, path);
            if (imagesFile != null && new File(imagesFile).exists()) {
                if (sharedQueue != null) {
                    return OMEZarrOpener.openFile(path, sharedQueue);
                }
                return OMEZarrOpener.openFile(imagesFile);
            }
        } catch (JDOMException | IOException e) {
            IJ.log(e.getMessage());
        }
        return null;
    }
}