package de.embl.cba.n5.util.openers;

import mpicbg.spim.data.SpimData;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class S3Opener extends BDVOpener {
    protected String serviceEndpoint;
    protected String signingRegion;
    protected String bucketName;
    protected String key;

    public S3Opener(final String serviceEndpoint, final String signingRegion, final String bucketName) {
        this.serviceEndpoint = serviceEndpoint;
        this.signingRegion = signingRegion;
        this.bucketName = bucketName;
        this.key = "";
    }

    public S3Opener(final String serviceEndpoint, final String signingRegion, final String bucketName, final String key) {
        this.serviceEndpoint = serviceEndpoint;
        this.signingRegion = signingRegion;
        this.bucketName = bucketName;
        this.key = key;
    }

    protected S3Opener(String url) {
        final String[] split = url.split("/");
        this.serviceEndpoint = Arrays.stream(split).limit(3).collect(Collectors.joining("/"));
        this.signingRegion = "us-west-2";
        this.bucketName = split[3];
        this.key = Arrays.stream(split).skip(4).collect(Collectors.joining("/"));
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getSigningRegion() {
        return signingRegion;
    }

    public void setSigningRegion(String signingRegion) {
        this.signingRegion = signingRegion;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SpimData readKey(final String key) throws IOException {
        return null;
    }
}
