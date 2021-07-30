package de.embl.cba.n5.util.openers;

import mpicbg.spim.data.SpimData;

import java.io.IOException;

public abstract class S3Opener extends BDVOpener {
    protected final String serviceEndpoint;
    protected final String signingRegion;
    protected final String bucketName;

    public S3Opener(final String serviceEndpoint, final String signingRegion, final String bucketName) {
        this.serviceEndpoint = serviceEndpoint;
        this.signingRegion = signingRegion;
        this.bucketName = bucketName;
    }

    public SpimData readKey(final String key) throws IOException {
        return null;
    }
}
