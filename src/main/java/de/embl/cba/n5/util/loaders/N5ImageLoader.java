/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2016 Tobias Pietzsch, Stephan Saalfeld, Stephan Preibisch,
 * Jean-Yves Tinevez, HongKee Moon, Johannes Schindelin, Curtis Rueden, John Bogovic
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
package de.embl.cba.n5.util.loaders;

import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.cache.VolatileGlobalCellCache;
import de.embl.cba.n5.util.N5CacheArrayLoader;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.MultiResolutionImgLoader;
import net.imglib2.Volatile;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.queue.FetcherThreads;
import net.imglib2.img.basictypeaccess.volatiles.array.*;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.*;
import net.imglib2.util.Cast;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static bdv.img.n5.BdvN5Format.DATA_TYPE_KEY;
import static bdv.img.n5.BdvN5Format.getPathName;

public class N5ImageLoader implements ViewerImgLoader, MultiResolutionImgLoader {
    public final N5Reader n5;
    /**
     * Maps setup id to {@link SetupImgLoader}.
     */
    public final Map<Integer, SetupImgLoader<?, ?>> setupImgLoaders = new HashMap<>();
    public BlockingFetchQueues<Callable<?>> queue;
    // TODO: it would be good if this would not be needed
    //       find available setups from the n5
    protected AbstractSequenceDescription<?, ?, ?> seq;
    private volatile boolean isOpen = false;
    private FetcherThreads fetchers;
    private VolatileGlobalCellCache cache;

    public N5ImageLoader(N5Reader n5Reader, AbstractSequenceDescription<?, ?, ?> sequenceDescription) {
        this.n5 = n5Reader;
        this.seq = sequenceDescription;
    }

    public N5ImageLoader(N5Reader n5Reader) {
        this.n5 = n5Reader;
    }

    public N5ImageLoader(N5Reader n5Reader, BlockingFetchQueues<Callable<?>> queue) {
        this.n5 = n5Reader;
        this.queue = queue;
    }

    public static SimpleCacheArrayLoader<?> createCacheArrayLoader(final N5Reader n5, final String pathName) throws IOException {
        final DatasetAttributes attributes = n5.getDatasetAttributes(pathName);
        switch (attributes.getDataType()) {
            case UINT8:
            case INT8:
                return new N5CacheArrayLoader<>(n5, pathName, attributes,
                        dataBlock -> new VolatileByteArray(Cast.unchecked(dataBlock.getData()), true));
            case UINT16:
            case INT16:
                return new N5CacheArrayLoader<>(n5, pathName, attributes,
                        dataBlock -> new VolatileShortArray(Cast.unchecked(dataBlock.getData()), true));
            case UINT32:
            case INT32:
                return new N5CacheArrayLoader<>(n5, pathName, attributes,
                        dataBlock -> new VolatileIntArray(Cast.unchecked(dataBlock.getData()), true));
            case UINT64:
            case INT64:
                return new N5CacheArrayLoader<>(n5, pathName, attributes,
                        dataBlock -> new VolatileLongArray(Cast.unchecked(dataBlock.getData()), true));
            case FLOAT32:
                return new N5CacheArrayLoader<>(n5, pathName, attributes,
                        dataBlock -> new VolatileFloatArray(Cast.unchecked(dataBlock.getData()), true));
            case FLOAT64:
                return new N5CacheArrayLoader<>(n5, pathName, attributes,
                        dataBlock -> new VolatileDoubleArray(Cast.unchecked(dataBlock.getData()), true));
            default:
                throw new IllegalArgumentException();
        }
    }

    protected void fetchSequenceDescriptionAndViewRegistrations() {
    }

    private void open() {
        if (!isOpen) {
            synchronized (this) {
                if (isOpen)
                    return;

                try {
                    int maxNumLevels = 0;
                    final List<? extends BasicViewSetup> setups = seq.getViewSetupsOrdered();
                    for (final BasicViewSetup setup : setups) {
                        final int setupId = setup.getId();
                        final SetupImgLoader<?, ?> setupImgLoader = createSetupImgLoader(setupId);
                        setupImgLoaders.put(setupId, setupImgLoader);
                        if (setupImgLoader != null) {
                            maxNumLevels = Math.max(maxNumLevels, setupImgLoader.numMipmapLevels());
                        }
                    }

                    final int numFetcherThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
                    final BlockingFetchQueues<Callable<?>> queue = new BlockingFetchQueues<>(maxNumLevels, numFetcherThreads);
                    fetchers = new FetcherThreads(queue, numFetcherThreads);
                    cache = new VolatileGlobalCellCache(queue);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                isOpen = true;
            }
        }
    }

    /**
     * Clear the cache. Images that were obtained from
     * this loader before {@link #close()} will stop working. Requesting images
     * after {@link #close()} will cause the n5 to be reopened (with a
     * new cache).
     */
    public void close() {
        if (isOpen) {
            synchronized (this) {
                if (!isOpen)
                    return;
                fetchers.shutdown();
                cache.clearCache();
                isOpen = false;
            }
        }
    }

    @Override
    public SetupImgLoader<?, ?> getSetupImgLoader(final int setupId) {
        open();
        return setupImgLoaders.get(setupId);
    }

    protected <T extends NativeType<T>, V extends Volatile<T> & NativeType<V>> SetupImgLoader<T, V> createSetupImgLoader(final int setupId) throws IOException {
        final String pathName = getPathName(setupId);
        final DataType dataType = n5.getAttribute(pathName, DATA_TYPE_KEY, DataType.class);
        switch (dataType) {
            case UINT8:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new UnsignedByteType(), new VolatileUnsignedByteType(), n5, cache));
            case UINT16:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new UnsignedShortType(), new VolatileUnsignedShortType(), n5, cache));
            case UINT32:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new UnsignedIntType(), new VolatileUnsignedIntType(), n5, cache));
            case UINT64:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new UnsignedLongType(), new VolatileUnsignedLongType(), n5, cache));
            case INT8:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new ByteType(), new VolatileByteType(), n5, cache));
            case INT16:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new ShortType(), new VolatileShortType(), n5, cache));
            case INT32:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new IntType(), new VolatileIntType(), n5, cache));
            case INT64:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new LongType(), new VolatileLongType(), n5, cache));
            case FLOAT32:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new FloatType(), new VolatileFloatType(), n5, cache));
            case FLOAT64:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new DoubleType(), new VolatileDoubleType(), n5, cache));
        }
        return null;
    }

    @Override
    public CacheControl getCacheControl() {
        open();
        return cache;
    }
}
