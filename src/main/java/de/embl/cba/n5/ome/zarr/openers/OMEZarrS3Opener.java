package de.embl.cba.n5.ome.zarr.openers;

import de.embl.cba.n5.ome.zarr.loaders.N5S3OMEZarrImageLoader;
import de.embl.cba.n5.util.openers.S3Opener;
import mpicbg.spim.data.SpimData;
import net.imglib2.util.Cast;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class OMEZarrS3Opener extends S3Opener {

    public OMEZarrS3Opener(String serviceEndpoint, String signingRegion, String bucketName) {
        super(serviceEndpoint, signingRegion, bucketName);
    }

    @Override
    public SpimData readKey(String key) throws IOException {
        N5S3OMEZarrImageLoader imageLoader = new N5S3OMEZarrImageLoader(serviceEndpoint, signingRegion, bucketName, key, ".");
        return new SpimData(null, Cast.unchecked(imageLoader.getSequenceDescription()), imageLoader.getViewRegistrations());
    }

    public static SpimData readURL(String url) throws IOException {
        final String[] split = url.split("/");
        String serviceEndpoint = Arrays.stream(split).limit(3).collect(Collectors.joining("/"));
        String signingRegion = "us-west-2";
        String bucketName = split[3];
        final String key = Arrays.stream(split).skip(4).collect(Collectors.joining("/"));
        final OMEZarrS3Opener reader = new OMEZarrS3Opener(serviceEndpoint, signingRegion, bucketName);
        return reader.readKey(key);
    }
}
