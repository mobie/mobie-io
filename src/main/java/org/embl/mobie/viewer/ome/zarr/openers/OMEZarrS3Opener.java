package org.embl.mobie.viewer.ome.zarr.openers;

import org.embl.mobie.viewer.ome.zarr.loaders.N5S3OMEZarrImageLoader;
import org.embl.mobie.viewer.util.openers.S3Opener;
import mpicbg.spim.data.SpimData;
import net.imglib2.util.Cast;

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

    @Override
    public SpimData readKey(String key) throws IOException {
        N5S3OMEZarrImageLoader imageLoader = new N5S3OMEZarrImageLoader(serviceEndpoint, signingRegion, bucketName, key, ".");
        return new SpimData(null, Cast.unchecked(imageLoader.getSequenceDescription()), imageLoader.getViewRegistrations());
    }
}
