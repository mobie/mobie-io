package org.embl.mobie.viewer.openorganelle;

import org.embl.mobie.viewer.util.ArrayCreator;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DataType;

import java.util.Arrays;

public class OrganelleArrayCreator<A, T extends NativeType<T>> extends ArrayCreator {
    public OrganelleArrayCreator(CellGrid cellGrid, DataType dataType) {
        super(cellGrid, dataType);
    }

    public A createArray(DataBlock<?> dataBlock, long[] gridPosition) {
        long[] cellDims = getCellDims(gridPosition);
        int n = (int) (cellDims[0] * cellDims[1] * cellDims[2]);
        return (A) VolatileDoubleArray(dataBlock, cellDims, n);
    }

    @Override
    public long[] getCellDims(long[] gridPosition) {
        long[] cellMin = new long[3];
        int[] cellDims = new int[3];
        cellGrid.getCellDimensions(gridPosition, cellMin, cellDims);
        return Arrays.stream(cellDims).mapToLong(i -> i).toArray(); // casting to long for creating ArrayImgs.*
    }
}