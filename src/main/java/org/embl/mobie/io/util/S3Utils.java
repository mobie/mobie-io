/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.io.util;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.HeadBucketResult;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.api.client.http.HttpStatusCodes;

import ij.gui.GenericDialog;

public abstract class S3Utils {
    private static String[] s3AccessAndSecretKey;

    public static void setS3AccessAndSecretKey(String[] s3AccessAndSecretKey) {
        S3Utils.s3AccessAndSecretKey = s3AccessAndSecretKey;
    }

    public static AmazonS3 getS3Client(String endpoint, String region, String bucket) {
        final AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        // first we create a client with anonymous credentials and see if we can access the bucket like this
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new AnonymousAWSCredentials());
        AmazonS3 s3 = AmazonS3ClientBuilder
            .standard()
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(endpointConfiguration)
            .withCredentials(credentialsProvider)
            .build();

        // check if we can access the access
        HeadBucketRequest headBucketRequest = new HeadBucketRequest(bucket);
        try {
            HeadBucketResult headBucketResult = s3.headBucket(headBucketRequest);
            return s3;
        } catch (AmazonServiceException e) {
            switch (e.getStatusCode()) {
                // if we get a 403 response (access forbidden), we try again with credentials
                case HttpStatusCodes.STATUS_CODE_FORBIDDEN:
                    if (s3AccessAndSecretKey != null) {
                        // use the given credentials
                        final BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessAndSecretKey[0], s3AccessAndSecretKey[1]);
                        credentialsProvider = new AWSStaticCredentialsProvider(credentials);
                    } else {
                        // look for credentials at other places
                        credentialsProvider = new DefaultAWSCredentialsProviderChain();
                        checkCredentialsExistence(credentialsProvider);
                    }
                    s3 = AmazonS3ClientBuilder
                        .standard()
                        .withPathStyleAccessEnabled(true)
                        .withEndpointConfiguration(endpointConfiguration)
                        .withCredentials(credentialsProvider)
                        .build();
                    // check if we have access permissions now
                    try {
                        HeadBucketResult headBucketResult = s3.headBucket(headBucketRequest);
                    } catch (AmazonServiceException e2) {
                        throw e2;
                    }
                    return s3;
                // otherwise the bucket does not exist or has been permanently moved; throw the exception
                default:
                    throw e;
            }
        }
    }

    public static AmazonS3 getS3Client(String uri) {
        final String endpoint = getEndpoint(uri);
        final String region = "us-west-2";  // TODO get region from uri
        final String[] bucketAndObject = getBucketAndObject(uri);
        return getS3Client(endpoint, region, bucketAndObject[0]);
    }

    public static void checkCredentialsExistence(AWSCredentialsProvider credentialsProvider) {
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new RuntimeException(e); // No credentials could be found
        }
    }

    public static String[] getBucketAndObject(String uri) {
        final String[] split = uri.split("/");
        String bucket = split[3];
        String object = Arrays.stream(split).skip(4).collect(Collectors.joining("/"));
        return new String[]{bucket, object};
    }

    public static String getEndpoint(String uri) {
        final String[] split = uri.split("/");
        String endpoint = Arrays.stream(split).limit(3).collect(Collectors.joining("/"));
        return endpoint;
    }

    public static String selectS3PathFromDirectory(String directory, String objectName) throws IOException {
        final String[] fileNames = getS3FileNames(directory);

        final GenericDialog gd = new GenericDialog("Select " + objectName);
        gd.addChoice(objectName, fileNames, fileNames[0]);
        gd.showDialog();
        if (gd.wasCanceled()) return null;
        final String fileName = gd.getNextChoice();
        String newFilePath = IOHelper.combinePath(directory, fileName);

        return newFilePath;
    }

    public static String[] getS3FileNames(String directory) {
        final ArrayList<String> filePaths = getS3FilePaths(directory);
        return filePaths.stream().map(File::new).map(File::getName).toArray(String[]::new);
    }

    public static ArrayList<String> getS3FilePaths(String directory) {
        final AmazonS3 s3 = getS3Client(directory);
        final String[] bucketAndObject = getBucketAndObject(directory);

        final String bucket = bucketAndObject[0];
        final String prefix = (bucketAndObject[1] == "") ? "" : (bucketAndObject[1] + "/");

        ListObjectsV2Request request = new ListObjectsV2Request()
            .withBucketName(bucket)
            .withPrefix(prefix)
            .withDelimiter("/");
        ListObjectsV2Result files = s3.listObjectsV2(request);
        final ArrayList<String> paths = new ArrayList<>();
        for (S3ObjectSummary summary : files.getObjectSummaries()) {
            paths.add(summary.getKey());
        }
        return paths;
    }

    public static boolean isS3(String directory) {
        return directory.contains("s3.amazon.aws.com") || directory.startsWith("https://s3");
    }
}
