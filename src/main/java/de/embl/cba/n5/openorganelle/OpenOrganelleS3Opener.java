package de.embl.cba.n5.openorganelle;

import de.embl.cba.n5.util.openers.S3Opener;
import mpicbg.spim.data.SpimData;
import net.imglib2.util.Cast;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class OpenOrganelleS3Opener extends S3Opener {

    public OpenOrganelleS3Opener(String serviceEndpoint, String signingRegion, String bucketName) {
        super(serviceEndpoint, signingRegion, bucketName);
    }

    public static SpimData readURL(String url) throws IOException {
        final String[] split = url.split("/");
        String serviceEndpoint = Arrays.stream(split).limit(3).collect(Collectors.joining("/"));
        String signingRegion = "us-west-2";
        String bucketName = split[3];
        final String key = Arrays.stream(split).skip(4).collect(Collectors.joining("/"));
        final OpenOrganelleS3Opener reader = new OpenOrganelleS3Opener(serviceEndpoint, signingRegion, bucketName);
        return reader.readKey(key);
    }

    @Override
    public SpimData readKey(String key) throws IOException {
        OpenOrganelleN5S3ImageLoader imageLoader = new OpenOrganelleN5S3ImageLoader(serviceEndpoint, signingRegion, bucketName, key);
        return new SpimData(null, Cast.unchecked(imageLoader.getSequenceDescription()), imageLoader.getViewRegistrations());
    }
}
