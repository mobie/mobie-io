package de.embl.cba.n5.util.loaders;

public interface S3ImageLoader {

    String getServiceEndpoint();

    String getSigningRegion();

    String getBucketName();

    String getKey();
}
