package org.embl.mobie.io.openorganelle;

import java.io.IOException;

import org.embl.mobie.io.n5.openers.S3Opener;

import mpicbg.spim.data.SpimData;
import net.imglib2.util.Cast;

public class OpenOrganelleS3Opener extends S3Opener {

    public OpenOrganelleS3Opener(String serviceEndpoint, String signingRegion, String bucketName) {
        super(serviceEndpoint, signingRegion, bucketName);
    }

    public OpenOrganelleS3Opener(String url) {
        super(url);
    }

    public static SpimData readURL(String url) throws IOException {
        final OpenOrganelleS3Opener reader = new OpenOrganelleS3Opener(url);
        return reader.readKey(reader.getKey());
    }

    @Override
    public SpimData readKey(String key) throws IOException {
        OpenOrganelleN5S3ImageLoader imageLoader = new OpenOrganelleN5S3ImageLoader(serviceEndpoint, signingRegion, bucketName, key);
        return new SpimData(null, Cast.unchecked(imageLoader.getSequenceDescription()), imageLoader.getViewRegistrations());
    }
}
