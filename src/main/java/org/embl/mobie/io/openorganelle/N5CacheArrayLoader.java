package org.embl.mobie.io.openorganelle;

import java.util.Arrays;

import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;

import bdv.img.cache.SimpleCacheArrayLoader;
import lombok.extern.slf4j.Slf4j;
import net.imglib2.img.cell.CellGrid;

@Slf4j
public class N5CacheArrayLoader<A> implements SimpleCacheArrayLoader<A> {
    private final N5Reader n5;
    private final String pathName;
    private final DatasetAttributes attributes;
    private final OrganelleArrayCreator<A, ?> arrayCreator;

    N5CacheArrayLoader(final N5Reader n5, final String pathName, final DatasetAttributes attributes, CellGrid grid) {
        this.n5 = n5;
        this.pathName = pathName;
        this.attributes = attributes;
        this.arrayCreator = new OrganelleArrayCreator<>(grid, attributes.getDataType());
    }

    @Override
    public A loadArray(final long[] gridPosition, int[] cellDimensions) {
        DataBlock<?> block = null;

        try {
            block = n5.readBlock(pathName, attributes, gridPosition);
        } catch (Exception e) {
            log.error("Error loading " + pathName + " at block " + Arrays.toString(gridPosition) + ": " + e);
        }

        if (block == null) {
            return (A) arrayCreator.createEmptyArray(gridPosition);
        } else {
            return arrayCreator.createArray(block, gridPosition);
        }
    }
}