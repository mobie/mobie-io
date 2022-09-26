/*
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
package org.embl.mobie.io.openorganelle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.jetbrains.annotations.NotNull;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.util.ConstantRandomAccessible;
import bdv.util.MipmapTransforms;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.Angle;
import mpicbg.spim.data.sequence.Channel;
import mpicbg.spim.data.sequence.DefaultVoxelDimensions;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.Illumination;
import mpicbg.spim.data.sequence.MultiResolutionImgLoader;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import mpicbg.spim.data.sequence.SequenceDescription;
import mpicbg.spim.data.sequence.Tile;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.ViewSetup;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.queue.FetcherThreads;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileByteType;
import net.imglib2.type.volatiles.VolatileDoubleType;
import net.imglib2.type.volatiles.VolatileFloatType;
import net.imglib2.type.volatiles.VolatileIntType;
import net.imglib2.type.volatiles.VolatileLongType;
import net.imglib2.type.volatiles.VolatileShortType;
import net.imglib2.type.volatiles.VolatileUnsignedByteType;
import net.imglib2.type.volatiles.VolatileUnsignedIntType;
import net.imglib2.type.volatiles.VolatileUnsignedLongType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;

@Slf4j
public class OpenOrganelleN5ImageLoader implements ViewerImgLoader, MultiResolutionImgLoader {
    public static boolean logging = false;
    protected final N5Reader n5;
    /**
     * Maps setup id to {@link SetupImgLoader}.
     */
    private final Map<Integer, SetupImgLoader<?, ?>> setupImgLoaders = new HashMap<>();
    private final Map<Integer, String> setupToPathname = new HashMap<>();
    private final Map<Integer, Multiscale> setupToMultiscale = new HashMap<>();
    private final Map<Integer, DatasetAttributes> setupToAttributes = new HashMap<>();
    private final Map<Integer, Integer> setupToChannel = new HashMap<>();
    private final Map<Integer, double[][]> setupToScales = new HashMap<>();
    protected AbstractSequenceDescription<?, ?, ?> seq;
    protected ViewRegistrations viewRegistrations;
    private volatile boolean isOpen = false;
    private FetcherThreads fetchers;
    private VolatileGlobalCellCache cache;
    private int sequenceTimepoints = 0;

    /**
     * The sequenceDescription and viewRegistrations are to be read from the n5 attributes.
     *
     * @param n5Reader
     */
    public OpenOrganelleN5ImageLoader(N5Reader n5Reader) {
        this.n5 = n5Reader;
        fetchSequenceDescriptionAndViewRegistrations();
    }

    private static SimpleCacheArrayLoader<?> createCacheArrayLoader(final N5Reader n5, final String pathName, CellGrid grid) throws IOException {
        final DatasetAttributes attributes = n5.getDatasetAttributes(pathName);
        return new N5CacheArrayLoader<>(n5, pathName, attributes, grid);
    }

    private void fetchSequenceDescriptionAndViewRegistrations() {
        try {
            initSetups();

            ArrayList<ViewSetup> viewSetups = new ArrayList<>();
            ArrayList<ViewRegistration> viewRegistrationList = new ArrayList<>();

            int numSetups = setupToMultiscale.size();
            for (int setupId = 0; setupId < numSetups; setupId++) {
                ViewSetup viewSetup = createViewSetup(setupId);
                int setupTimepoints = 1;
                sequenceTimepoints = Math.max(setupTimepoints, sequenceTimepoints);
                viewSetups.add(viewSetup);
                viewRegistrationList.addAll(createViewRegistrations(setupId, setupTimepoints));
            }

            viewRegistrations = new ViewRegistrations(viewRegistrationList);

            seq = new SequenceDescription(new TimePoints(createTimePoints(sequenceTimepoints)), viewSetups);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private ArrayList<TimePoint> createTimePoints(int sequenceTimepoints) {
        ArrayList<TimePoint> timePoints = new ArrayList<>();
        for (int t = 0; t < sequenceTimepoints; t++) {
            timePoints.add(new TimePoint(t));
        }
        return timePoints;
    }

    private void initSetups() throws IOException {
        int setupId = -1;
        double[][] scales = n5.getAttribute("", "scales", double[][].class);
        Multiscale multiscale = getMultiscale(""); // returns multiscales[ 0 ]
        DatasetAttributes attributes = getDatasetAttributes(multiscale.datasets[0].path);
        long nC = 1;

        for (int c = 0; c < nC; c++) {
            // each channel is one setup
            setupId++;
            setupToChannel.put(setupId, c);

            // all channels have the same multiscale and attributes
            setupToMultiscale.put(setupId, multiscale);
            setupToAttributes.put(setupId, attributes);
            setupToPathname.put(setupId, "");
            setupToScales.put(setupId, scales);
        }

        List<String> labels = n5.getAttribute("labels", "labels", List.class);
        if (labels != null) {
            for (String label : labels) {
                setupId++;
                setupToChannel.put(setupId, 0); // TODO: https://github.com/ome/ngff/issues/19
                String pathName = "labels/" + label;
                multiscale = getMultiscale(pathName);
                attributes = getDatasetAttributes(pathName + "/" + multiscale.datasets[0].path);

                setupToMultiscale.put(setupId, multiscale);
                setupToAttributes.put(setupId, attributes);
                setupToPathname.put(setupId, pathName);
            }
        }
    }

    /**
     * The dataType, number of channels and number of timepoints are stored
     * in the different pyramid levels (datasets).
     * According to the spec all datasets must be indentical in that sense
     * and we thus fetch this information from level 0.
     * <p>
     * In addition, level 0 contains the information about the size of the full resolution image.
     *
     * @param pathName
     * @return
     * @throws IOException
     */
    private DatasetAttributes getDatasetAttributes(String pathName) throws IOException {
        return n5.getDatasetAttributes(pathName);
    }

    /**
     * The primary use case for multiple multiscales at the moment (the one listed in the spec)
     * is multiple different downsamplings.
     * A base image with two multiscales each with a different scale or a different method.
     * <p>
     * I don't know a good logic right now how to deal with different pyramids,
     * thus I just fetch the first one, i.e. multiscales[ 0 ].
     * <p>
     * ??? There's no need for the two multiscales to have the same base though.
     * ??? So it would also allow you to just have two pyramids (in our jargon) in the same zgroup.
     *
     * @param pathName
     * @return
     * @throws IOException
     */
    private Multiscale getMultiscale(String pathName) throws IOException {
        final String key = "multiscales";
        Multiscale[] multiscales = n5.getAttribute(pathName, key, Multiscale[].class);
        if (multiscales == null) {
            String location = "";
            location += "; path: " + pathName;
            location += "; attribute: " + key;
            throw new UnsupportedOperationException("Could not find multiscales at " + location);
        } else {
            for (Multiscale multiscale : multiscales) {
                multiscale.name = n5.getAttribute("", "name", String.class);
            }
        }
        return multiscales[0];
    }

    public AbstractSequenceDescription<?, ?, ?> getSequenceDescription() {
        //open();
        seq.setImgLoader(Cast.unchecked(this));
        return seq;
    }

    public ViewRegistrations getViewRegistrations() {
        return viewRegistrations;
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

    @NotNull
    private ArrayList<ViewRegistration> createViewRegistrations(int setupId, int setupTimepoints) {
        Multiscale multiscale = setupToMultiscale.get(setupId);
        AffineTransform3D transform = new AffineTransform3D();

        double[] scale = multiscale.datasets[0].transform.scale;
        transform.scale(scale[0] / 1000, scale[1] / 1000, scale[2] / 1000);

        ArrayList<ViewRegistration> viewRegistrations = new ArrayList<>();
        for (int t = 0; t < setupTimepoints; t++)
            viewRegistrations.add(new ViewRegistration(t, setupId, transform));

        return viewRegistrations;
    }

    private ViewSetup createViewSetup(int setupId) {
        final DatasetAttributes attributes = setupToAttributes.get(setupId);
        FinalDimensions dimensions = new FinalDimensions(attributes.getDimensions());
        Multiscale multiscale = setupToMultiscale.get(setupId);
        VoxelDimensions voxelDimensions = readVoxelDimensions(multiscale);
        Tile tile = new Tile(0);

        Channel channel;
        if (setupToPathname.get(setupId).contains("labels"))
            channel = new Channel(setupToChannel.get(setupId), "labels");
        else
            channel = new Channel(setupToChannel.get(setupId));

        Angle angle = new Angle(0);
        Illumination illumination = new Illumination(0);
        String name = readName(multiscale, setupId);
        //if ( setupToPathname.get( setupId ).contains( "labels" ))
        //	viewSetup.setAttribute( new ImageType( ImageType.Type.IntensityImage ) );
        return new ViewSetup(setupId, name, dimensions, voxelDimensions, tile, channel, angle, illumination);
    }

    private String readName(Multiscale multiscale, int setupId) {
        if (multiscale.name != null)
            return multiscale.name;
        else
            return "image " + setupId;
    }

    @NotNull
    private VoxelDimensions readVoxelDimensions(Multiscale multiscale) {
        try {
            double[] voxeldim = {multiscale.datasets[0].transform.scale[0] / 1000, multiscale.datasets[0].transform.scale[1] / 1000, multiscale.datasets[0].transform.scale[2] / 1000};
            return new FinalVoxelDimensions(multiscale.datasets[0].transform.units[0], voxeldim);
        } catch (Exception e) {
            return new DefaultVoxelDimensions(3);
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

    private <T extends NativeType<T>, V extends Volatile<T> & NativeType<V>> SetupImgLoader<T, V> createSetupImgLoader(final int setupId) throws IOException {
        switch (setupToAttributes.get(setupId).getDataType()) {
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

    private static class Multiscale {
        String name;
        double[][] scales;
        Dataset[] datasets;
    }

    private static class Dataset {
        String path;
        Transform transform;
    }

    private static class Transform {
        String[] axes;
        double[] scale;
        double[] translate;
        String[] units;
    }

    private class SetupImgLoader<T extends NativeType<T>, V extends Volatile<T> & NativeType<V>>
        extends AbstractViewerSetupImgLoader<T, V>
        implements MultiResolutionSetupImgLoader<T> {
        private final int setupId;

        private final double[][] mipmapResolutions;

        private final AffineTransform3D[] mipmapTransforms;

        public SetupImgLoader(final int setupId, final T type, final V volatileType) {
            super(type, volatileType);
            this.setupId = setupId;
            mipmapResolutions = readMipmapResolutions();
            mipmapTransforms = new AffineTransform3D[mipmapResolutions.length];
            for (int level = 0; level < mipmapResolutions.length; level++)
                mipmapTransforms[level] = MipmapTransforms.getMipmapTransformDefault(mipmapResolutions[level]);
        }

        /**
         * @return
         */
        private double[][] readMipmapResolutions() {
            return setupToScales.get(setupId);
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
            final String pathName = getPathName(setupId, level);
            try {
                final DatasetAttributes attributes = getDatasetAttributes(pathName);
                return new FinalDimensions(attributes.getDimensions());
            } catch (Exception e) {
                throw new RuntimeException("Could not read from " + pathName);
            }
        }

        @NotNull
        public String getPathName(int setupId, int level) {
            return setupToPathname.get(setupId) + "/" + setupToMultiscale.get(this.setupId).datasets[level].path;
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
        private <N extends NativeType<N>> RandomAccessibleInterval<N> prepareCachedImage(final int timepointId, final int level, final LoadingStrategy loadingStrategy, final N type) {
            try {
                final String pathName = getPathName(setupId, level);
                final DatasetAttributes attributes = getDatasetAttributes(pathName);

                if (logging) {
                    log.info("Preparing image " + pathName + " of data type " + attributes.getDataType());
                }

                // ome.zarr is 5D but BDV expects 3D
                final long[] dimensions = Arrays.stream(attributes.getDimensions()).limit(3).toArray();
                final int[] cellDimensions = Arrays.stream(attributes.getBlockSize()).limit(3).toArray();
                final CellGrid grid = new CellGrid(dimensions, cellDimensions);

                final int priority = numMipmapLevels() - 1 - level;
                final CacheHints cacheHints = new CacheHints(loadingStrategy, priority, false);

                final SimpleCacheArrayLoader<?> loader = createCacheArrayLoader(n5, pathName, grid);
                return cache.createImg(grid, timepointId, setupId, level, cacheHints, loader, type);
            } catch (IOException e) {
                log.error(String.format("image data for timepoint %d setup %d level %d could not be found.%n",
                    timepointId, setupId, level));
                return Views.interval(
                    new ConstantRandomAccessible<>(type.createVariable(), 3),
                    new FinalInterval(1, 1, 1));
            }
        }
    }

}
