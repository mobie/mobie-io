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
package org.embl.mobie.io.ome.zarr.openers;

import java.io.IOException;

import org.embl.mobie.io.n5.openers.S3Opener;
import org.embl.mobie.io.ome.zarr.loaders.N5OMEZarrImageLoader;
import org.embl.mobie.io.ome.zarr.loaders.N5S3OMEZarrImageLoader;

import bdv.cache.SharedQueue;
import mpicbg.spim.data.SpimData;
import net.imglib2.util.Cast;

public class OMEZarrS3Opener extends S3Opener {

    public OMEZarrS3Opener(String serviceEndpoint, String signingRegion, String bucketName) {
        super(serviceEndpoint, signingRegion, bucketName);
    }

    public OMEZarrS3Opener(String url) {
        super(url);
    }

    public static SpimData readURL(String url) throws IOException {
        final OMEZarrS3Opener reader = new OMEZarrS3Opener(url);
        return reader.readKey(reader.getKey());
    }

    public static SpimData readURL(String url, SharedQueue sharedQueue) throws IOException {
        final OMEZarrS3Opener reader = new OMEZarrS3Opener(url);
        return reader.readKey(reader.getKey(), sharedQueue);
    }

    public SpimData readKey(String key, SharedQueue sharedQueue) throws IOException {
        N5OMEZarrImageLoader.logging = logging;
        N5S3OMEZarrImageLoader imageLoader = new N5S3OMEZarrImageLoader(serviceEndpoint, signingRegion, bucketName, key, ".", sharedQueue);
        return new SpimData(null, Cast.unchecked(imageLoader.getSequenceDescription()), imageLoader.getViewRegistrations());
    }

    @Override
    public SpimData readKey(String key) throws IOException {
        N5OMEZarrImageLoader.logging = logging;
        N5S3OMEZarrImageLoader imageLoader = new N5S3OMEZarrImageLoader(serviceEndpoint, signingRegion, bucketName, key, ".");
        return new SpimData(null, Cast.unchecked(imageLoader.getSequenceDescription()), imageLoader.getViewRegistrations());
    }
}
