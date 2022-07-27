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
package org.embl.mobie.io.ome.zarr.loaders;

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
import mpicbg.spim.data.sequence.*;
import net.imglib2.*;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.queue.FetcherThreads;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
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
import org.embl.mobie.io.ome.zarr.readers.N5OmeZarrReader;
import org.embl.mobie.io.ome.zarr.readers.N5S3OmeZarrReader;
import org.embl.mobie.io.ome.zarr.util.N5OMEZarrCacheArrayLoader;
import org.embl.mobie.io.ome.zarr.util.OmeZarrMultiscales;
import org.embl.mobie.io.ome.zarr.util.ZarrAxes;
import org.embl.mobie.io.ome.zarr.util.ZarrAxis;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

import static org.embl.mobie.io.ome.zarr.util.OmeZarrMultiscales.MULTI_SCALE_KEY;

@Slf4j
public class N5OMEZarrImageLoader implements ViewerImgLoader, MultiResolutionImgLoader {

    private static final int C = 3;
    private static final int T = 4;
    public static boolean logging = false;
    public final N5Reader n5;
    /**
     * Maps setup id to {@link SetupImgLoader}.
     */
    private final Map<Integer, SetupImgLoader<?, ?>> setupImgLoaders = new HashMap<>();
    private final Map<Integer, String> setupToPathname = new HashMap<>();
    private final Map<Integer, OmeZarrMultiscales> setupToMultiscale = new HashMap<>();
    private final Map<Integer, DatasetAttributes> setupToAttributes = new HashMap<>();
    private final Map<Integer, Integer> setupToChannel = new HashMap<>();
    protected AbstractSequenceDescription<?, ?, ?> seq;
    protected ViewRegistrations viewRegistrations;
    private volatile boolean isOpen = false;
    private int sequenceTimepoints = 0;
    private FetcherThreads fetchers;
    private VolatileGlobalCellCache cache;
    private ZarrAxes zarrAxes;
    List<ZarrAxis> zarrAxesList;
    private BlockingFetchQueues<Callable<?>> queue;

    /**
     * The sequenceDescription and viewRegistrations are known already, typically read from xml.
     *
     * @param n5Reader            reader
     * @param sequenceDescription
     */
    @Deprecated
    public N5OMEZarrImageLoader(N5Reader n5Reader, AbstractSequenceDescription<?, ?, ?> sequenceDescription) {
        this.n5 = n5Reader;
        this.seq = sequenceDescription; // TODO: it is better to fetch from within Zarr
        try {
            initSetups();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public N5OMEZarrImageLoader(N5Reader n5Reader) {
        this.n5 = n5Reader;
        fetchSequenceDescriptionAndViewRegistrations();
    }

    public N5OMEZarrImageLoader(N5Reader n5Reader, BlockingFetchQueues<Callable<?>> queue) {
        this.n5 = n5Reader;
        this.queue = queue;
        fetchSequenceDescriptionAndViewRegistrations();
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
                if ( zarrAxes.hasTimepoints() )
                    setupTimepoints = (int) setupToAttributes.get(setupId).getDimensions()[zarrAxes.timeIndex()];
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
        OmeZarrMultiscales[] multiscales = getMultiscale(""); // returns multiscales[ 0 ]
        for (OmeZarrMultiscales multiscale : multiscales) {
            DatasetAttributes attributes = getDatasetAttributes(multiscale.datasets[0].path);

            // TODO: Maybe AbstractOmeZarrReader which has .getAxes()
            zarrAxes = n5 instanceof N5OmeZarrReader ? ((N5OmeZarrReader) n5).getAxes() :
                    n5 instanceof N5S3OmeZarrReader ? ((N5S3OmeZarrReader) n5).getAxes() : ZarrAxes.NOT_SPECIFIED;
            zarrAxesList = n5 instanceof N5OmeZarrReader ? ((N5OmeZarrReader) n5).getZarrAxes() :
                    n5 instanceof N5S3OmeZarrReader ? ((N5S3OmeZarrReader) n5).getZarrAxes() : null;

            multiscale.axes = zarrAxes;
            multiscale.zarrAxisList = zarrAxesList;

            long nC = 1;
            if (zarrAxes.hasChannels()) {
                nC = attributes.getDimensions()[zarrAxes.channelIndex()];
            }

            for (int c = 0; c < nC; c++) {
                // each channel is one setup
                setupId++;
                setupToChannel.put(setupId, c);

                // all channels have the same multiscale and attributes
                setupToMultiscale.put(setupId, multiscale);
                setupToAttributes.put(setupId, attributes);
                setupToPathname.put(setupId, "");
            }
        }

        List<String> labels = n5.getAttribute("labels", "labels", List.class);
        if (labels != null) {
            for (String label : labels) {
                setupId++;
                setupToChannel.put(setupId, 0); // TODO: https://github.com/ome/ngff/issues/19
                String pathName = "labels/" + label;
                multiscales = getMultiscale(pathName);
                for (OmeZarrMultiscales multiscale : multiscales) {
                    DatasetAttributes attributes = getDatasetAttributes(pathName + "/" + multiscale.datasets[0].path);

                    zarrAxes = n5 instanceof N5OmeZarrReader ? ((N5OmeZarrReader) n5).getAxes() :
                            n5 instanceof N5S3OmeZarrReader ? ((N5S3OmeZarrReader) n5).getAxes() : ZarrAxes.NOT_SPECIFIED;
                    zarrAxesList = n5 instanceof N5OmeZarrReader ? ((N5OmeZarrReader) n5).getZarrAxes() :
                            n5 instanceof N5S3OmeZarrReader ? ((N5S3OmeZarrReader) n5).getZarrAxes() : null;

                    multiscale.axes = zarrAxes;
                    multiscale.zarrAxisList = zarrAxesList;

                    setupToMultiscale.put(setupId, multiscale);
                    setupToAttributes.put(setupId, attributes);
                    setupToPathname.put(setupId, pathName);
                }
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
    private OmeZarrMultiscales[] getMultiscale(String pathName) throws IOException {
        OmeZarrMultiscales[] multiscales = n5.getAttribute(pathName, MULTI_SCALE_KEY, OmeZarrMultiscales[].class);
        if (multiscales == null) {
            String location = "";
            if (n5 instanceof N5S3OmeZarrReader) {
                final N5S3OmeZarrReader s3ZarrReader = (N5S3OmeZarrReader) n5;
                s3ZarrReader.setDimensionSeparator("/");
                location += "service endpoint: " + s3ZarrReader.getServiceEndpoint();
                location += "; bucket: " + s3ZarrReader.getBucketName();
                location += "; container path: " + s3ZarrReader.getContainerPath();
                location += "; path: " + pathName;
                location += "; attribute: " + MULTI_SCALE_KEY;
            } else {
                location += " path: " + pathName;
            }
            throw new UnsupportedOperationException("Could not find multiscales at " + location);
        }
        return multiscales;
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

    @NotNull
    private ArrayList<ViewRegistration> createViewRegistrations(int setupId, int setupTimePoints) {
        ArrayList<ViewRegistration> viewRegistrations = new ArrayList<>();
        // 3D transform of full resolution level
        AffineTransform3D transform = getAffineTransform3D(setupId, 0);
        for (int t = 0; t < setupTimePoints; t++) {
            viewRegistrations.add(new ViewRegistration(t, setupId, transform));
        }

        return viewRegistrations;
    }

    private double[] getXYZScale( OmeZarrMultiscales.CoordinateTransformations coordinateTransformations ) {
        double[] scale = coordinateTransformations.scale;
        double[] xyzScale = null;
        if (scale != null && zarrAxesList != null) {
            int scalesFirstIndexBackward = scale.length - 1;
            if (zarrAxes.hasChannels()) {
                if (zarrAxes.is3DWithChannels() || zarrAxes.is2D()) {
                    xyzScale = new double[]{
                            scale[scalesFirstIndexBackward],
                            scale[scalesFirstIndexBackward - 1],
                            1.0
                    };
                } else {
                    xyzScale = new double[]{
                            scale[scalesFirstIndexBackward],
                            scale[scalesFirstIndexBackward - 1],
                            scale[scalesFirstIndexBackward - 2]};
                }
            } else {
                if (zarrAxes.containsXYZCoordinates()) {
                    xyzScale = new double[]{
                            scale[scalesFirstIndexBackward],
                            scale[scalesFirstIndexBackward - 1],
                            scale[scalesFirstIndexBackward - 2]};
                } else {
                    xyzScale = new double[]{
                            scale[scalesFirstIndexBackward],
                            scale[scalesFirstIndexBackward - 1],
                            1.0
                    };
                }
            }
        }
        return xyzScale;
    }

    @NotNull
    private AffineTransform3D getAffineTransform3D(int setupId, int datasetId) {
        OmeZarrMultiscales multiscales = setupToMultiscale.get(setupId);
        AffineTransform3D transform = new AffineTransform3D();
        if (multiscales.datasets[datasetId].coordinateTransformations != null) {
            double[] scale = getXYZScale(multiscales.datasets[datasetId].coordinateTransformations[0]);
            if (scale != null) {
                transform.scale(scale[0], scale[1], scale[2]);
            }

            double[] translation = multiscales.datasets[datasetId].coordinateTransformations[0].translation;
            if (translation != null) {
                transform.translate(translation);
            }
        }
        return transform;
    }

    private VoxelDimensions getVoxelDimensions3D( int setupId, int datasetId ) {
        OmeZarrMultiscales multiscales = setupToMultiscale.get(setupId);

        if (multiscales.datasets[datasetId].coordinateTransformations != null && zarrAxesList != null) {
            double[] scale = getXYZScale( multiscales.datasets[datasetId].coordinateTransformations[0] );
            // get unit of last dimension, under assumption this is the X axis (i.e. a space axis)
            String unit = zarrAxesList.get(zarrAxesList.size() - 1).getUnit();

            if (scale != null && unit != null) {
                if (zarrAxes.is2D() || zarrAxes.is3DWithChannels() ) {
                    return new FinalVoxelDimensions(unit, new double[]{scale[0], scale[1], 1.0});
                } else {
                    return new FinalVoxelDimensions(unit, scale);
                }
            }
        }

        return new DefaultVoxelDimensions(3);
    }

    private Dimensions getSpatialDimensions( DatasetAttributes attributes )
    {
        final long[] spatialDimensions = new long[ 3 ];
        long[] attributeDimensions = attributes.getDimensions();
        Arrays.fill( spatialDimensions, 1 );
        final Map< Integer, Integer > spatialToZarr = zarrAxes.spatialToZarr();
        for ( Map.Entry< Integer, Integer > entry : spatialToZarr.entrySet() )
        {
            spatialDimensions[ entry.getKey() ] = attributeDimensions[ entry.getValue() ];
        }
        return new FinalDimensions( spatialDimensions );
    }

    private ViewSetup createViewSetup(int setupId) {
        OmeZarrMultiscales multiscale = setupToMultiscale.get(setupId);
        Dimensions dimensions = getSpatialDimensions( setupToAttributes.get( setupId ) );
        VoxelDimensions voxelDimensions = getVoxelDimensions3D(setupId, 0);

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

    // TODO: remove
    private long[] getDimensions(DatasetAttributes attributes) {
        if (zarrAxes != null) {
            if ((zarrAxes.equals(ZarrAxes.YX)) || (zarrAxes.is4DWithTimepointsAndChannels())) {
                return fillDimensions(attributes);
            }
        }
        return Arrays.stream(attributes.getDimensions()).limit(3).toArray();
    }

    private long[] fillDimensions(DatasetAttributes attributes) {
        long[] tmp = new long[3];
        tmp[0] = Arrays.stream(attributes.getDimensions()).toArray()[0];
        tmp[1] = Arrays.stream(attributes.getDimensions()).toArray()[1];
        tmp[2] = 1;
        return tmp;
    }

    // TODO: Add description
    private int[] getBlockSize(DatasetAttributes attributes) {
        if (zarrAxes.hasZAxis()) {
            return fillBlockSize(attributes);
        }
        else {
            return Arrays.stream(attributes.getBlockSize()).limit(3).toArray();
        }
    }

    private int[] fillBlockSize(DatasetAttributes attributes) {
        int[] tmp = new int[3];
        tmp[0] = Arrays.stream(attributes.getBlockSize()).toArray()[0];
        tmp[1] = Arrays.stream(attributes.getBlockSize()).toArray()[1];
        tmp[2] = 1;
        return tmp;
    }

    private SimpleCacheArrayLoader<?> createCacheArrayLoader(final N5Reader n5, final String pathName, int channel, int timepointId, CellGrid grid) throws IOException {
        final DatasetAttributes attributes = n5.getDatasetAttributes(pathName);
        return new N5OMEZarrCacheArrayLoader<>(n5, pathName, channel, timepointId, attributes, grid, zarrAxes);
    }

    private class SetupImgLoader<T extends NativeType<T>, V extends Volatile<T> & NativeType<V>>
            extends AbstractViewerSetupImgLoader<T, V>
            implements MultiResolutionSetupImgLoader<T> {
        private final int setupId;

        private final double[][] mipmapResolutions;

        private final AffineTransform3D[] mipmapTransforms;

        public SetupImgLoader(final int setupId, final T type, final V volatileType) throws IOException {
            super(type, volatileType);
            this.setupId = setupId;
            mipmapResolutions = readMipmapResolutions();
            mipmapTransforms = new AffineTransform3D[mipmapResolutions.length];
            for (int level = 0; level < mipmapResolutions.length; level++) {
                mipmapTransforms[level] = MipmapTransforms.getMipmapTransformDefault(mipmapResolutions[level]);
            }
        }

        /**
         * @return
         * @throws IOException
         */
        private double[][] readMipmapResolutions() throws IOException {
            OmeZarrMultiscales multiscale = setupToMultiscale.get(setupId);
            double[][] mipmapResolutions = new double[multiscale.datasets.length][];

            long[] dimensionsOfLevel0 = getDatasetAttributes(getPathName(setupId, 0)).getDimensions();
            mipmapResolutions[0] = new double[]{1.0, 1.0, 1.0};

            for (int level = 1; level < mipmapResolutions.length; level++) {
                long[] dimensions = getDatasetAttributes(getPathName(setupId, level)).getDimensions();
                mipmapResolutions[level] = new double[3];
                if (multiscale.axes.is2D() || multiscale.axes.is3DWithChannels()) {
                    for (int d = 0; d < 2; d++) {
                        mipmapResolutions[level][d] = Math.round(1.0 * dimensionsOfLevel0[d] / dimensions[d]);
                    }
                    mipmapResolutions[level][2] = 1.0;
                } else {
                    for (int d = 0; d < 3; d++) {
                        mipmapResolutions[level][d] = 1.0 * dimensionsOfLevel0[d] / dimensions[d];
                    }
                }
            }

            return mipmapResolutions;
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
        private <T extends NativeType<T>> RandomAccessibleInterval<T> prepareCachedImage(final int timepointId, final int level, final LoadingStrategy loadingStrategy, final T type) {
            try {
                final String pathName = getPathName(setupId, level);
                final DatasetAttributes attributes = getDatasetAttributes(pathName);

                if (logging) {
                    log.info("Preparing image " + pathName + " of data type " + attributes.getDataType());
                }

                // The below dimensions refer to one time point and one channel.
                // That means they are either 2D or 3D.
                long[] dimensions = getSpatialDimensions(attributes).dimensionsAsLongArray();
                final int[] cellDimensions = getBlockSize(attributes);
                final CellGrid grid = new CellGrid(dimensions, cellDimensions);

                final int priority = numMipmapLevels() - 1 - level;
                final CacheHints cacheHints = new CacheHints(loadingStrategy, priority, false);

                final SimpleCacheArrayLoader<?> loader = createCacheArrayLoader(n5, pathName, setupToChannel.get(setupId), timepointId, grid);
                return cache.createImg(grid, timepointId, setupId, level, cacheHints, loader, type);
            } catch (IOException e) {
                log.error(String.format(
                        "image data for timepoint %d setup %d level %d could not be found.%n",
                        timepointId, setupId, level));
                return Views.interval(
                        new ConstantRandomAccessible<>(type.createVariable(), 3),
                        new FinalInterval(1, 1, 1));
            }
        }
    }
}
