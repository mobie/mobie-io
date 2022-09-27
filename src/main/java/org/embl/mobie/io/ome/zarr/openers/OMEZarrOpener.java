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
package org.embl.mobie.io.ome.zarr.openers;

import java.io.File;
import java.io.IOException;

import org.embl.mobie.io.n5.openers.BDVOpener;
import org.embl.mobie.io.ome.zarr.loaders.N5OMEZarrImageLoader;
import org.embl.mobie.io.ome.zarr.readers.N5OmeZarrReader;

import com.google.gson.GsonBuilder;

import bdv.cache.SharedQueue;
import mpicbg.spim.data.SpimData;
import net.imglib2.util.Cast;

public class OMEZarrOpener extends BDVOpener {
    private final String filePath;

    public OMEZarrOpener(String filePath) {
        this.filePath = filePath;
    }

    public static SpimData openFile(String filePath) throws IOException {
        OMEZarrOpener omeZarrOpener = new OMEZarrOpener(filePath);
        return omeZarrOpener.readFile();
    }

    public static SpimData openFile(String filePath, SharedQueue sharedQueue) throws IOException {
        N5OMEZarrImageLoader.logging = logging;
        OMEZarrOpener omeZarrOpener = new OMEZarrOpener(filePath);
        return omeZarrOpener.readFile(sharedQueue);
    }

    private SpimData readFile(SharedQueue sharedQueue) throws IOException {
        N5OMEZarrImageLoader.logging = logging;
        N5OmeZarrReader reader = new N5OmeZarrReader(this.filePath, new GsonBuilder());
        N5OMEZarrImageLoader imageLoader = new N5OMEZarrImageLoader(reader, sharedQueue);
        return new SpimData(
            new File(this.filePath),
            Cast.unchecked(imageLoader.getSequenceDescription()),
            imageLoader.getViewRegistrations());
    }

    private SpimData readFile() throws IOException {
        N5OMEZarrImageLoader.logging = logging;
        N5OmeZarrReader reader = new N5OmeZarrReader(this.filePath, new GsonBuilder());
        N5OMEZarrImageLoader imageLoader = new N5OMEZarrImageLoader(reader);
        return new SpimData(
            new File(this.filePath),
            Cast.unchecked(imageLoader.getSequenceDescription()),
            imageLoader.getViewRegistrations());
    }

}
