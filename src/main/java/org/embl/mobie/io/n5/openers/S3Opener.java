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
package org.embl.mobie.io.n5.openers;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import mpicbg.spim.data.SpimData;
import org.embl.mobie.io.OpenerLogging;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;

public class S3Opener extends OpenerLogging
{
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
        url = url.replaceAll( "\\s", "" );
        url = url.trim();
        final String[] split = url.split("/");
        this.serviceEndpoint = Arrays.stream(split).limit(3).collect(Collectors.joining("/"));
        this.signingRegion = null; // "us-west-2";
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
