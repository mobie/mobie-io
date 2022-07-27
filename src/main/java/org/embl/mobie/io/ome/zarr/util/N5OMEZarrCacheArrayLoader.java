package org.embl.mobie.io.ome.zarr.util;

import bdv.img.cache.SimpleCacheArrayLoader;
import com.amazonaws.SdkClientException;
import lombok.extern.slf4j.Slf4j;
import net.imglib2.img.cell.CellGrid;
import org.embl.mobie.io.n5.util.N5DataTypeSize;
import org.embl.mobie.io.ome.zarr.loaders.N5OMEZarrImageLoader;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public class N5OMEZarrCacheArrayLoader<A> implements SimpleCacheArrayLoader<A> {
    private final N5Reader n5;
    private final String pathName;
    private final int channel;
    private final int timepoint;
    private final DatasetAttributes attributes;
    private final ZarrArrayCreator<A, ?> zarrArrayCreator;
    private final ZarrAxes zarrAxes;

    public N5OMEZarrCacheArrayLoader(final N5Reader n5, final String pathName, final int channel, final int timepoint, final DatasetAttributes attributes, CellGrid grid, ZarrAxes zarrAxes) {
        this.n5 = n5;
        this.pathName = pathName; // includes the level
        this.channel = channel;
        this.timepoint = timepoint;
        this.attributes = attributes;
        final DataType dataType = attributes.getDataType();
        this.zarrArrayCreator = new ZarrArrayCreator<>(grid, dataType, zarrAxes);
        this.zarrAxes = zarrAxes;
    }

    @Override
    public A loadArray(final long[] gridPosition) throws IOException {
        DataBlock<?> block = null;

        long[] dataBlockIndices = toZarrChunkIndices(gridPosition);

        long start = 0;
        if (N5OMEZarrImageLoader.logging)
            start = System.currentTimeMillis();

        try {
            block = n5.readBlock(pathName, attributes, dataBlockIndices);
        } catch (SdkClientException e) {
            log.error(e.getMessage()); // this happens sometimes, not sure yet why...
        }
        if (N5OMEZarrImageLoader.logging) {
            if (block != null)
            {
                final long millis = System.currentTimeMillis() - start;
                final int numElements = block.getNumElements();
                final DataType dataType = attributes.getDataType();
                final float megaBytes = (float) numElements * N5DataTypeSize.getNumBytesPerElement( dataType ) / 1000000.0F;
                final float mbPerSecond = megaBytes / ( millis / 1000.0F );
                log.info( pathName + " " + Arrays.toString( dataBlockIndices ) + ": " + "Read " + numElements + " " + dataType + " (" + String.format( "%.3f", megaBytes ) + " MB) in " + millis + " ms (" + String.format( "%.3f", mbPerSecond ) + " MB/s)." );
            }
            else
                log.warn(pathName + " " + Arrays.toString(dataBlockIndices) + ": Missing, returning zeros.");
        }

        if (block == null) {
            return (A) zarrArrayCreator.createEmptyArray( gridPosition );
        } else {
            return zarrArrayCreator.createArray(block, gridPosition);
        }
    }

    private long[] toZarrChunkIndices( long[] gridPosition) {

        long[] chunkInZarr = new long[ zarrAxes.getNumDimension() ];

        // fill in the spatial dimensions
        final Map< Integer, Integer > spatialToZarr = zarrAxes.spatialToZarr();
        for ( Map.Entry< Integer, Integer > entry : spatialToZarr.entrySet() )
            chunkInZarr[ entry.getValue() ] = gridPosition[ entry.getKey() ];

        if ( zarrAxes.hasChannels() )
            chunkInZarr[ zarrAxes.channelIndex() ] = channel;

        if ( zarrAxes.hasTimepoints() )
            chunkInZarr[ zarrAxes.timeIndex() ] = timepoint;

        return chunkInZarr;
    }
}