/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2023 EMBL
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

import com.amazonaws.auth.*;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.*;

import ij.gui.GenericDialog;
import org.jetbrains.annotations.NotNull;

public abstract class S3Utils
{
    private static String[] s3AccessAndSecretKey;
    private static boolean useCredentialsChain;

    public static void setS3AccessAndSecretKey( String[] s3AccessAndSecretKey ) {
        S3Utils.s3AccessAndSecretKey = s3AccessAndSecretKey;
    }

    public static String[] getS3AccessAndSecretKey()
    {
        return s3AccessAndSecretKey;
    }

    // look for credentials at common places
    // on the client computer
    public static void useS3Credenditals( boolean b ) {
        useCredentialsChain = b;
    }

    public static AmazonS3 getS3Client( String endpoint, String region )
    {
        final AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        AWSCredentialsProvider credentialsProvider = getAwsCredentialsProvider();

        // Create the access
        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled( true )
                .withEndpointConfiguration( endpointConfiguration )
                .withCredentials( credentialsProvider )
                .build();

        return s3;
    }

    @NotNull
    public static AWSCredentialsProvider getAwsCredentialsProvider()
    {
        if ( s3AccessAndSecretKey != null)
        {
            // Use given credentials
            final BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessAndSecretKey[0], s3AccessAndSecretKey[1]);
            return new AWSStaticCredentialsProvider(credentials);
        }
        else if ( useCredentialsChain )
        {
            // Look for credentials
            DefaultAWSCredentialsProviderChain credentialsProviderChain = new DefaultAWSCredentialsProviderChain();
            checkCredentialsExistence( credentialsProviderChain );
            return credentialsProviderChain;
        }
        else
        {
            // Anonymous
           return new AWSStaticCredentialsProvider( new AnonymousAWSCredentials() );
        }
    }

    public static AmazonS3 getS3Client( String uri ) {
        final String endpoint = getEndpoint( uri );
        // final String region = "us-west-2";
        //final String[] bucketAndObject = getBucketAndObject(uri);
        return getS3Client( endpoint, null );
    }

    public static void checkCredentialsExistence( AWSCredentialsProvider credentialsProvider ) {
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new RuntimeException(e); // No credentials could be found
        }
    }

    public static String[] getBucketAndObject( String uri ) {
        final String[] split = uri.split("/");
        String bucket = split[3];
        String object = Arrays.stream(split).skip(4).collect(Collectors.joining("/"));
        return new String[]{bucket, object};
    }

    public static String getEndpoint( String uri ) {
        final String[] split = uri.split("/");
        String endpoint = Arrays.stream(split).limit(3).collect(Collectors.joining("/"));
        return endpoint;
    }

    public static String selectS3PathFromDirectory( String directory, String objectName ) throws IOException {
        final String[] fileNames = getS3FileNames(directory);

        final GenericDialog gd = new GenericDialog("Select " + objectName);
        gd.addChoice(objectName, fileNames, fileNames[0]);
        gd.showDialog();
        if (gd.wasCanceled()) return null;
        final String fileName = gd.getNextChoice();
        String newFilePath = IOHelper.combinePath(directory, fileName);

        return newFilePath;
    }

    public static String[] getS3FileNames( String directory ) {
        final ArrayList<String> filePaths = getS3FilePaths(directory);
        return filePaths.stream().map(File::new).map(File::getName).toArray(String[]::new);
    }

    public static ArrayList<String> getS3FilePaths( String directory ) {
        final AmazonS3 s3 = getS3Client( directory );
        final String[] bucketAndObject = getBucketAndObject(directory);

        final String bucket = bucketAndObject[0];
        final String prefix = bucketAndObject[ 1 ].isEmpty() ? "" : ( bucketAndObject[1] + "/" );

        final ArrayList<String> paths = new ArrayList<>();
        S3Objects.withPrefix( s3, bucket, prefix ).forEach( (S3ObjectSummary objectSummary ) -> {
            String path = IOHelper.combinePath( directory, objectSummary.getKey().replace( prefix, "" ) );
            paths.add( path );
        });
        return paths;
    }

    public static boolean isS3( String directory ) {
        return directory.contains("s3.amazon.aws.com") || directory.startsWith("https://s3");
    }

    public static String getURI( String serviceEndpoint, String bucketName, String key )
    {
        String uri = IOHelper.combinePath( serviceEndpoint, bucketName, key );
        return uri;
    }
}
