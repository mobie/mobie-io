package org.embl.mobie.io.util;

import java.util.Arrays;

public class ChunkSizeComputer
{
    private final int[] dimensionsXYCZT;
    private final int bytesPerPixel;

    public ChunkSizeComputer( int[] dimensionsXYCZT, int bytesPerPixel )
    {
        this.dimensionsXYCZT = dimensionsXYCZT;
        this.bytesPerPixel = bytesPerPixel;
    }

    /**
     * This attempts to make the shape of the XYZ chunks similar to the shape of the XYZ volume.
     *
     * @param bytesPerChunk
     * @return
     */
    public int[] getChunkDimensionsXYCZT( int bytesPerChunk )
    {
        double pixelsPerChunk = (double) bytesPerChunk / bytesPerPixel;

        int nx = dimensionsXYCZT[ 0 ];
        int ny = dimensionsXYCZT[ 1 ];
        int nz = dimensionsXYCZT[ 3 ];

        double pixelsPerVolume = (double) nx * ny * nz;

        int cz = ( int ) Math.min( nz, Math.max( 1, Math.round( Math.pow( pixelsPerChunk / pixelsPerVolume, 1. / 3 ) * nz ) ) );
        int cx = ( int ) Math.min( nx, Math.max( 1, Math.round( Math.pow( pixelsPerChunk / cz, 1. / 2 ) ) ) );
        int cy = Math.min( ny, cx );

        return new int[]{ cx, cy, 1, cz, 1 };
    }

    public static void main( String[] args )
    {
        int[][] imageDimensions = {
                { 2000, 2000, 1, 100, 1 },
                { 2000, 2000, 1, 1, 1 },
                { 2000, 2000, 1, 3, 10 },
                { 2000, 2000, 3, 2000, 10} };

        System.out.println( "ImageJ dimension order: X Y C Z T");
        for ( int i = 0; i < imageDimensions.length; i++ )
        {
            int[] chunkDimensionsXYCZT = new ChunkSizeComputer( imageDimensions[ i ], 1 ).getChunkDimensionsXYCZT( 8000000 );
            System.out.println( "Image: " + Arrays.toString( imageDimensions[ i ] ) + ", Chunks: " + Arrays.toString( chunkDimensionsXYCZT ) );
        }
    }

}
