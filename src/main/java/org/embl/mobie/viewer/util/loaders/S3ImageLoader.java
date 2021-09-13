package org.embl.mobie.viewer.util.loaders;

public interface S3ImageLoader {

    String getServiceEndpoint();

    String getSigningRegion();

    String getBucketName();

    String getKey();
}
