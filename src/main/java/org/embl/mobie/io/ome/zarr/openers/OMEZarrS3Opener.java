package org.embl.mobie.io.ome.zarr.openers;

import bdv.util.volatiles.SharedQueue;
import mpicbg.spim.data.SpimData;
import net.imglib2.util.Cast;
import org.embl.mobie.io.n5.openers.S3Opener;
import org.embl.mobie.io.ome.zarr.loaders.N5S3OMEZarrImageLoader;

import java.io.IOException;

public class OMEZarrS3Opener extends S3Opener {

    public OMEZarrS3Opener(String serviceEndpoint, String signingRegion, String bucketName) {
        super(serviceEndpoint, signingRegion, bucketName);
    }

    public OMEZarrS3Opener(String url) {
        super(url);
    }

    public static SpimData readURL(String url) throws IOException {
        final OMEZarrS3Opener reader = new OMEZarrS3Opener(url);
        return reader.readKey(reader.getKey());
    }

    public static SpimData readURL(String url, SharedQueue sharedQueue) throws IOException {
        final OMEZarrS3Opener reader = new OMEZarrS3Opener(url);
        return reader.readKey(reader.getKey(), sharedQueue);
    }

    public SpimData readKey(String key, SharedQueue sharedQueue) throws IOException {
        N5S3OMEZarrImageLoader imageLoader = new N5S3OMEZarrImageLoader(serviceEndpoint, signingRegion, bucketName, key, ".");
        return new SpimData(null, Cast.unchecked(imageLoader.getSequenceDescription()), imageLoader.getViewRegistrations());
    }

    @Override
    public SpimData readKey(String key) throws IOException {
        N5S3OMEZarrImageLoader imageLoader = new N5S3OMEZarrImageLoader(serviceEndpoint, signingRegion, bucketName, key, ".");
        return new SpimData(null, Cast.unchecked(imageLoader.getSequenceDescription()), imageLoader.getViewRegistrations());
    }
}
