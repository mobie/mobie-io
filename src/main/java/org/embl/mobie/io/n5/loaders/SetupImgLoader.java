package org.embl.mobie.io.n5.loaders;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;

import bdv.AbstractViewerSetupImgLoader;
import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.util.ConstantRandomAccessible;
import bdv.util.MipmapTransforms;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.view.Views;

import static bdv.img.n5.BdvN5Format.DOWNSAMPLING_FACTORS_KEY;
import static bdv.img.n5.BdvN5Format.getPathName;

@Slf4j
public class SetupImgLoader<T extends NativeType<T>, V extends Volatile<T> & NativeType<V>>
    extends AbstractViewerSetupImgLoader<T, V>
    implements MultiResolutionSetupImgLoader<T> {
    private final int setupId;
    private final double[][] mipmapResolutions;
    private final AffineTransform3D[] mipmapTransforms;
    protected N5Reader n5;
    private VolatileGlobalCellCache cache;

    public SetupImgLoader(final int setupId, final T type, final V volatileType, N5Reader n5, VolatileGlobalCellCache cache) throws IOException {
        super(type, volatileType);
        this.n5 = n5;
        this.cache = cache;
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
            return new FinalDimensions(attributes.getDimensions());
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
    private <N extends NativeType<N>> RandomAccessibleInterval<N> prepareCachedImage(final int timepointId, final int level, final LoadingStrategy loadingStrategy, final N type) {
        try {
            final String pathName = getPathName(setupId, timepointId, level);
            final DatasetAttributes attributes = n5.getDatasetAttributes(pathName);
            final long[] dimensions = attributes.getDimensions();
            final int[] cellDimensions = attributes.getBlockSize();
            final CellGrid grid = new CellGrid(dimensions, cellDimensions);

            final int priority = numMipmapLevels() - 1 - level;
            final CacheHints cacheHints = new CacheHints(loadingStrategy, priority, false);

            final SimpleCacheArrayLoader<?> loader = N5ImageLoader.createCacheArrayLoader(n5, pathName);
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