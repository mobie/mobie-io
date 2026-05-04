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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import com.google.api.client.http.HttpStatusCodes;
import ij.gui.GenericDialog;
import org.jetbrains.annotations.NotNull;

public abstract class S3Utils
{
    private static String[] s3AccessAndSecretKey;
    private static boolean useCredentialsChain;

    // This is, unf., still needed to open data that are not images
    public static void setS3AccessAndSecretKey( String[] s3AccessAndSecretKey ) {
        S3Utils.s3AccessAndSecretKey = s3AccessAndSecretKey;
    }

    // This is, unf., still needed to open data that are not images
    public static String[] getS3AccessAndSecretKey()
    {
        return s3AccessAndSecretKey;
    }

    // look for credentials at common places on the client computer
    // FIXME: This is never called (also not from mobie-viewer-fiji
    public static void useS3Credentials( boolean b )
    {
        useCredentialsChain = b;
    }

    public static S3Client buildS3Client( String endpoint, String region )
    {
        return createS3Client( endpoint, region, getAwsCredentialsProvider() );
    }

    @NotNull
    public static AwsCredentialsProvider getAwsCredentialsProvider()
    {
        if ( s3AccessAndSecretKey != null)
        {
            // Use given credentials
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create( s3AccessAndSecretKey[0], s3AccessAndSecretKey[1] ) );
        }
        else if ( useCredentialsChain ) // FIXME: How to set this boolean?
        {
            // Look for credentials
            final DefaultCredentialsProvider credentialsProviderChain = DefaultCredentialsProvider.create();
            checkCredentialsExistence( credentialsProviderChain );
            return credentialsProviderChain;
        }
        else
        {
            // Anonymous
            return AnonymousCredentialsProvider.create();
        }
    }

    // https://imagesc.zulipchat.com/#narrow/channel/328251-NGFF/topic/AWS.20Credentials
    // old code:
    // https://github.com/mobie/mobie-io/blob/hackathon_prague_2022/src/main/java/org/embl/mobie/io/util/S3Utils.java#L63C2-L111C6
    public static S3Client buildS3Client( String endpoint, String region, String bucket )
    {
        // first we create a client with anonymous credentials and see if we can access the bucket like this
        AwsCredentialsProvider credentialsProvider = AnonymousCredentialsProvider.create();
        S3Client s3 = createS3Client( endpoint, region, credentialsProvider );

        // check if we can access the bucket
        final HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket( bucket ).build();
        try {
            s3.headBucket( headBucketRequest );
            return s3;
        } catch ( S3Exception e ) {
            switch ( e.statusCode() ) {
                // if we get a 403 response (access forbidden), we try again with credentials
                case HttpStatusCodes.STATUS_CODE_FORBIDDEN:
                    if ( s3AccessAndSecretKey != null ) {
                        // use the given credentials
                        credentialsProvider = StaticCredentialsProvider.create(
                                AwsBasicCredentials.create( s3AccessAndSecretKey[0], s3AccessAndSecretKey[1] ) );
                    } else {
                        // look for credentials at other places
                        credentialsProvider = DefaultCredentialsProvider.create();
                        checkCredentialsExistence( credentialsProvider );
                    }

                    s3 = createS3Client( endpoint, region, credentialsProvider );

                    // check if we can access now
                    s3.headBucket( headBucketRequest );
                    return s3;
                // otherwise the bucket does not exist or has been permanently moved; throw the exception
                default:
                    throw e;
            }
        }
    }

    public static Map< String, S3Client > locationToS3Client = new ConcurrentHashMap<>();

    public static S3Client getS3Client( String uri ) {
        final String endpoint = getEndpoint( uri );
        String[] bucketAndObject = getBucketAndObject( uri );
        String key = endpoint + "/" + bucketAndObject[0];
        if ( ! locationToS3Client.containsKey( key ) )
        {
            // It takes some time to build the client,
            // thus we cache it.
            S3Client s3Client = buildS3Client( endpoint, null, bucketAndObject[ 0 ] );
            locationToS3Client.put( key, s3Client );
        }
        return locationToS3Client.get( key );
    }

    public static void checkCredentialsExistence( AwsCredentialsProvider credentialsProvider ) {
        try {
            credentialsProvider.resolveCredentials();
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
        final S3Client s3 = getS3Client( directory );
        final String[] bucketAndObject = getBucketAndObject(directory);

        final String bucket = bucketAndObject[0];
        final String prefix = bucketAndObject[ 1 ].isEmpty() ? "" : ( bucketAndObject[1] + "/" );

        final ArrayList<String> paths = new ArrayList<>();
        final ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket( bucket )
                .prefix( prefix )
                .build();
        for ( ListObjectsV2Response response : s3.listObjectsV2Paginator( request ) )
        {
            for ( S3Object objectSummary : response.contents() )
            {
                String path = IOHelper.combinePath( directory, objectSummary.key().replace( prefix, "" ) );
                paths.add( path );
            }
        }
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

    private static S3Client createS3Client( String endpoint, String region, AwsCredentialsProvider credentialsProvider )
    {
        final S3ClientBuilder builder = S3Client.builder()
                .serviceConfiguration( S3Configuration.builder().pathStyleAccessEnabled( true ).build() )
                .credentialsProvider( credentialsProvider )
                .region( resolveRegion( region ) );

        if ( endpoint != null && !endpoint.isEmpty() )
            builder.endpointOverride( URI.create( endpoint ) );

        return builder.build();
    }

    private static Region resolveRegion( String region )
    {
        return region == null || region.isEmpty() ? Region.US_EAST_1 : Region.of( region );
    }
}
