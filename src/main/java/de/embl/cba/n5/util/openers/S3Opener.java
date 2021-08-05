package de.embl.cba.n5.util.openers;

import mpicbg.spim.data.SpimData;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class S3Opener extends BDVOpener {
    protected final String serviceEndpoint;
    protected final String signingRegion;
    protected final String bucketName;
    protected final String key;

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
//
//    public S3Opener setupOpenerParametersForURL(String url){
//        final String[] split = url.split("/");
//        String serviceEndpoint = Arrays.stream(split).limit(3).collect(Collectors.joining("/"));
//        String signingRegion = "us-west-2";
//        String bucketName = split[3];
//        String key = Arrays.stream(split).skip(4).collect(Collectors.joining("/"));
//        return new S3Opener(serviceEndpoint, signingRegion, bucketName, key);
//    }


    public SpimData readKey(final String key) throws IOException {
        return null;
    }
}
