package org.embl.mobie.io.n5.util;

import java.util.Arrays;
import java.util.function.Function;

import org.janelia.saalfeldlab.n5.ByteArrayDataBlock;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.DoubleArrayDataBlock;
import org.janelia.saalfeldlab.n5.FloatArrayDataBlock;
import org.janelia.saalfeldlab.n5.IntArrayDataBlock;
import org.janelia.saalfeldlab.n5.LongArrayDataBlock;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.ShortArrayDataBlock;

import bdv.img.cache.SimpleCacheArrayLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class N5CacheArrayLoader<A> implements SimpleCacheArrayLoader<A> {
    private final N5Reader n5;
    private final String pathName;
    private final DatasetAttributes attributes;
    private final Function<DataBlock<?>, A> createArray;

    public N5CacheArrayLoader(final N5Reader n5, final String pathName, final DatasetAttributes attributes, final Function<DataBlock<?>, A> createArray) {
        this.n5 = n5;
        this.pathName = pathName;
        this.attributes = attributes;
        this.createArray = createArray;
    }

    @Override
    public A loadArray(final long[] gridPosition) {
        DataBlock<?> block = null;

        try {
            block = n5.readBlock(pathName, attributes, gridPosition);
        } catch (Exception e) {
            log.error("Error loading " + pathName + " at block " + Arrays.toString(gridPosition) + ": " + e);
        }

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