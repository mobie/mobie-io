package org.embl.mobie.io.n5.util;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.volatiles.array.*;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.util.Cast;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.imglib2.N5CellLoader;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public abstract class ArrayCreator<A, T extends NativeType<T>> {
    protected final CellGrid cellGrid;
    protected final DataType dataType;
    protected final BiConsumer<ArrayImg<T, ?>, DataBlock<?>> copyFromBlock;

    public ArrayCreator(CellGrid cellGrid, DataType dataType) {
        this.cellGrid = cellGrid;
        this.dataType = dataType;
        this.copyFromBlock = N5CellLoader.createCopy(dataType);
    }

    @NotNull
    public A VolatileDoubleArray(DataBlock<?> dataBlock, long[] cellDims, int n) {
        switch (dataType) {
            case UINT8:
            case INT8:
                byte[] bytes = new byte[n];
                copyFromBlock.accept(Cast.unchecked(ArrayImgs.bytes(bytes, cellDims)), dataBlock);
                return (A) new VolatileByteArray(bytes, true);
            case UINT16:
            case INT16:
                short[] shorts = new short[n];
                copyFromBlock.accept(Cast.unchecked(ArrayImgs.shorts(shorts, cellDims)), dataBlock);
                return (A) new VolatileShortArray(shorts, true);
            case UINT32:
            case INT32:
                int[] ints = new int[n];
                copyFromBlock.accept(Cast.unchecked(ArrayImgs.ints(ints, cellDims)), dataBlock);
                return (A) new VolatileIntArray(ints, true);
            case UINT64:
            case INT64:
                long[] longs = new long[n];
                copyFromBlock.accept(Cast.unchecked(ArrayImgs.longs(longs, cellDims)), dataBlock);
                return (A) new VolatileLongArray(longs, true);
            case FLOAT32:
                float[] floats = new float[n];
                copyFromBlock.accept(Cast.unchecked(ArrayImgs.floats(floats, cellDims)), dataBlock);
                return (A) new VolatileFloatArray(floats, true);
            case FLOAT64:
                double[] doubles = new double[n];
                copyFromBlock.accept(Cast.unchecked(ArrayImgs.doubles(doubles, cellDims)), dataBlock);
                return (A) new VolatileDoubleArray(doubles, true);
            default:
                throw new IllegalArgumentException();
        }
    }

    public A createEmptyArray(long[] gridPosition) {
        long[] cellDims = getCellDims(gridPosition);
        int n = (int) (cellDims[0] * cellDims[1] * cellDims[2]);
        switch (dataType) {
            case UINT8:
            case INT8:
                return Cast.unchecked(new VolatileByteArray(new byte[n], true));
            case UINT16:
            case INT16:
                return Cast.unchecked(new VolatileShortArray(new short[n], true));
            case UINT32:
            case INT32:
                return Cast.unchecked(new VolatileIntArray(new int[n], true));
            case UINT64:
            case INT64:
                return Cast.unchecked(new VolatileLongArray(new long[n], true));
            case FLOAT32:
                return Cast.unchecked(new VolatileFloatArray(new float[n], true));
            case FLOAT64:
                return Cast.unchecked(new VolatileDoubleArray(new double[n], true));
            default:
                throw new IllegalArgumentException();
        }
    }

    public long[] getCellDims(long[] gridPosition) {
        return null;
    }
}