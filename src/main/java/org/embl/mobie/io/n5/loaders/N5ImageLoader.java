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
package org.embl.mobie.io.n5.loaders;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.util.ConstantRandomAccessible;
import bdv.util.MipmapTransforms;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.MultiResolutionImgLoader;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import mpicbg.spim.data.sequence.SequenceDescription;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.*;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.queue.FetcherThreads;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.img.basictypeaccess.volatiles.array.*;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.*;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;
import org.embl.mobie.io.ome.zarr.util.OmeZarrMultiscales;
import org.janelia.saalfeldlab.n5.*;
import org.jdom2.Element;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static bdv.img.n5.BdvN5Format.*;
import static mpicbg.spim.data.XmlKeys.BASEPATH_TAG;

@Slf4j
public class N5ImageLoader implements ViewerImgLoader, MultiResolutionImgLoader {
    protected final N5Reader n5;
    /**
     * Maps setup id to {@link SetupImgLoader}.
     */
    private final Map<Integer, SetupImgLoader> setupImgLoaders = new HashMap<>();
    // TODO: it would be good if this would not be needed
    //       find available setups from the n5
    protected AbstractSequenceDescription<?, ?, ?> seq;
    protected ViewRegistrations viewRegistrations;
    private volatile boolean isOpen = false;
    private FetcherThreads fetchers;
    private VolatileGlobalCellCache cache;
    private BlockingFetchQueues<Callable<?>> queue;


    public N5ImageLoader(N5Reader n5Reader, AbstractSequenceDescription<?, ?, ?> sequenceDescription) {
        this.n5 = n5Reader;
        this.seq = sequenceDescription;
    }

    public N5ImageLoader(N5Reader n5Reader, AbstractSequenceDescription<?, ?, ?> sequenceDescription, BlockingFetchQueues<Callable<?>> queue) {
        this.n5 = n5Reader;
        this.seq = sequenceDescription;
        this.queue = queue;
    }

    private static File loadBasePath(final Element root, final File xmlFile) {
        File xmlFileParentDirectory = xmlFile.getParentFile();
        if (xmlFileParentDirectory == null)
            xmlFileParentDirectory = new File(".");
        return XmlHelpers.loadPath(root, BASEPATH_TAG, ".", xmlFileParentDirectory);
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

    public AbstractSequenceDescription<?, ?, ?> getSequenceDescription() {
        //open();
        seq.setImgLoader(Cast.unchecked(this));
        return seq;
    }

    public ViewRegistrations getViewRegistrations() {
        return viewRegistrations;
    }

    public void setViewRegistrations(ViewRegistrations viewRegistrations) {
        this.viewRegistrations = viewRegistrations;
    }

    private DatasetAttributes getDatasetAttributes(String pathName) throws IOException {
        return n5.getDatasetAttributes(pathName);
    }

    public void setSeq(SequenceDescription seq) {
        this.seq = seq;
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
                        final SetupImgLoader setupImgLoader = createSetupImgLoader(setupId);
                        setupImgLoaders.put(setupId, setupImgLoader);
                        maxNumLevels = Math.max(maxNumLevels, setupImgLoader.numMipmapLevels());
                    }
                    if (queue == null) {
                        final int numFetcherThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
                        queue = new BlockingFetchQueues<>(maxNumLevels, numFetcherThreads);
                        fetchers = new FetcherThreads(queue, numFetcherThreads);
                    }
                    cache = new VolatileGlobalCellCache(queue);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                isOpen = true;
            }
        }
    }

    private String readName(OmeZarrMultiscales multiscale, int setupId) {
        if (multiscale.name != null)
            return multiscale.name;
        else
            return "image " + setupId;
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
                if (fetchers != null)
                    fetchers.shutdown();
                cache.clearCache();
                isOpen = false;
            }
        }
    }

    @Override
    public SetupImgLoader getSetupImgLoader(final int setupId) {
        open();
        return setupImgLoaders.get(setupId);
    }

    private <T extends NativeType<T>, V extends Volatile<T> & NativeType<V>> SetupImgLoader<T, V> createSetupImgLoader(final int setupId) throws IOException {
        final String pathName = getPathName(setupId);
        final DataType dataType = n5.getAttribute(pathName, DATA_TYPE_KEY, DataType.class);
        switch (dataType) {
            case UINT8:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new UnsignedByteType(), new VolatileUnsignedByteType()));
            case UINT16:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new UnsignedShortType(), new VolatileUnsignedShortType()));
            case UINT32:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new UnsignedIntType(), new VolatileUnsignedIntType()));
            case UINT64:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new UnsignedLongType(), new VolatileUnsignedLongType()));
            case INT8:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new ByteType(), new VolatileByteType()));
            case INT16:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new ShortType(), new VolatileShortType()));
            case INT32:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new IntType(), new VolatileIntType()));
            case INT64:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new LongType(), new VolatileLongType()));
            case FLOAT32:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new FloatType(), new VolatileFloatType()));
            case FLOAT64:
                return Cast.unchecked(new SetupImgLoader<>(setupId, new DoubleType(), new VolatileDoubleType()));
        }
        return null;
    }

    @Override
    public CacheControl getCacheControl() {
        open();
        return cache;
    }

    private static class N5CacheArrayLoader<A> implements SimpleCacheArrayLoader<A> {
        private final N5Reader n5;
        private final String pathName;
        private final DatasetAttributes attributes;
        private final Function<DataBlock<?>, A> createArray;

        N5CacheArrayLoader(final N5Reader n5, final String pathName, final DatasetAttributes attributes, final Function<DataBlock<?>, A> createArray) {
            this.n5 = n5;
            this.pathName = pathName;
            this.attributes = attributes;
            this.createArray = createArray;
        }

        @Override
        public A loadArray(final long[] gridPosition) throws IOException {
            DataBlock<?> block = null;

            try {
                block = n5.readBlock(pathName, attributes, gridPosition);
            } catch (Exception e) {
                log.error("Error loading " + pathName + " at block " + Arrays.toString(gridPosition) + ": " + e);
            }

//			if ( block != null )
//				log.warn( pathName + " " + Arrays.toString( gridPosition ) + " " + block.getNumElements() );
//			else
//				log.warn( pathName + " " + Arrays.toString( gridPosition ) + " NaN" );


            if (block == null) {
                final int[] blockSize = attributes.getBlockSize();
                final int n = blockSize[0] * blockSize[1] * blockSize[2];
                switch (attributes.getDataType()) {
                    case UINT8:
                    case INT8:
                        return createArray.apply(new ByteArrayDataBlock(blockSize, gridPosition, new byte[n]));
                    case UINT16:
                    case INT16:
                        return createArray.apply(new ShortArrayDataBlock(blockSize, gridPosition, new short[n]));
                    case UINT32:
                    case INT32:
                        return createArray.apply(new IntArrayDataBlock(blockSize, gridPosition, new int[n]));
                    case UINT64:
                    case INT64:
                        return createArray.apply(new LongArrayDataBlock(blockSize, gridPosition, new long[n]));
                    case FLOAT32:
                        return createArray.apply(new FloatArrayDataBlock(blockSize, gridPosition, new float[n]));
                    case FLOAT64:
                        return createArray.apply(new DoubleArrayDataBlock(blockSize, gridPosition, new double[n]));
                    default:
                        throw new IllegalArgumentException();
                }
            } else {
                return createArray.apply(block);
            }
        }
    }

    public class SetupImgLoader<T extends NativeType<T>, V extends Volatile<T> & NativeType<V>>
            extends AbstractViewerSetupImgLoader<T, V>
            implements MultiResolutionSetupImgLoader<T> {
        private final int setupId;

        private final double[][] mipmapResolutions;

        private final AffineTransform3D[] mipmapTransforms;

        public SetupImgLoader(final int setupId, final T type, final V volatileType) throws IOException {
            super(type, volatileType);
            this.setupId = setupId;
            final String pathName = getPathName(setupId);
            mipmapResolutions = n5.getAttribute(pathName, DOWNSAMPLING_FACTORS_KEY, double[][].class);
            mipmapTransforms = new AffineTransform3D[mipmapResolutions.length];
            for (int level = 0; level < mipmapResolutions.length; level++)
                mipmapTransforms[level] = MipmapTransforms.getMipmapTransformDefault(mipmapResolutions[level]);
        }

        @Override
        public RandomAccessibleInterval<V> getVolatileImage(final int timepointId, final int level, final ImgLoaderHint... hints) {
            return prepareCachedImage(timepointId, level, LoadingStrategy.BUDGETED, volatileType);
        }

        @Override
        public RandomAccessibleInterval<T> getImage(final int timepointId, final int level, final ImgLoaderHint... hints) {
            return prepareCachedImage(timepointId, level, LoadingStrategy.BLOCKING, type);
        }

        @Override
        public Dimensions getImageSize(final int timepointId, final int level) {
            try {
                final String pathName = getPathName(setupId, timepointId, level);
                final DatasetAttributes attributes = n5.getDatasetAttributes(pathName);
                FinalDimensions dimensions = new FinalDimensions(attributes.getDimensions());
                return dimensions;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public double[][] getMipmapResolutions() {
            return mipmapResolutions;
        }

        @Override
        public AffineTransform3D[] getMipmapTransforms() {
            return mipmapTransforms;
        }

        @Override
        public int numMipmapLevels() {
            return mipmapResolutions.length;
        }

        @Override
        public VoxelDimensions getVoxelSize(final int timepointId) {
            return null;
        }

        /**
         * Create a {@link CellImg} backed by the cache.
         */
        private <T extends NativeType<T>> RandomAccessibleInterval<T> prepareCachedImage(final int timepointId, final int level, final LoadingStrategy loadingStrategy, final T type) {
            try {
                final String pathName = getPathName(setupId, timepointId, level);
                final DatasetAttributes attributes = n5.getDatasetAttributes(pathName);
                final long[] dimensions = attributes.getDimensions();
                final int[] cellDimensions = attributes.getBlockSize();
                final CellGrid grid = new CellGrid(dimensions, cellDimensions);

                final int priority = numMipmapLevels() - 1 - level;
                final CacheHints cacheHints = new CacheHints(loadingStrategy, priority, false);

                final SimpleCacheArrayLoader<?> loader = createCacheArrayLoader(n5, pathName);
                return cache.createImg(grid, timepointId, setupId, level, cacheHints, loader, type);
            } catch (IOException e) {
                log.error(String.format(
                        "image data for timepoint %d setup %d level %d could not be found.",
                        timepointId, setupId, level));
                return Views.interval(
                        new ConstantRandomAccessible<>(type.createVariable(), 3),
                        new FinalInterval(1, 1, 1));
            }
        }
    }
}
