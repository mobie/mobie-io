package org.embl.mobie.io.n5.loaders;

public interface S3ImageLoader {

    String getServiceEndpoint();

    String getSigningRegion();

    String getBucketName();

    String getKey();
}
