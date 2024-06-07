package org.embl.mobie.io.util;

import bdv.util.AbstractSource;
import bdv.viewer.Interpolation;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

import java.util.Arrays;

public class RandomAccessibleIntervalSource4D< T extends NumericType< T > > extends AbstractSource< T >
{
    private final RandomAccessibleInterval< T > source;

    protected int currentTimePointIndex;

    private RandomAccessibleInterval< T > currentSource;

    private final RealRandomAccessible< T >[] currentInterpolatedSources;

    private final AffineTransform3D sourceTransform;

    public RandomAccessibleIntervalSource4D(
            final RandomAccessibleInterval< T > img,
            final T type,
            final AffineTransform3D sourceTransform,
            final VoxelDimensions voxelDimensions,
            final String name )
    {
        super( type, name, voxelDimensions );
        this.source = img;
        this.sourceTransform = sourceTransform;
        currentInterpolatedSources = new RealRandomAccessible[ Interpolation.values().length ];
        loadTimepoint( 0 );
    }

    private void loadTimepoint( final int timepointIndex )
    {
        currentTimePointIndex = timepointIndex;
        if ( isPresent( timepointIndex ) )
        {
            final T zero = getType().createVariable();
            zero.setZero();
            currentSource = Views.hyperSlice( source, 3, timepointIndex );
            for ( final Interpolation method : Interpolation.values() )
                currentInterpolatedSources[ method.ordinal() ] = Views.interpolate( Views.extendValue( currentSource, zero ), interpolators.get( method ) );
        }
        else
        {
            currentSource = null;
            Arrays.fill( currentInterpolatedSources, null );
        }
    }

    @Override
    public boolean isPresent( final int t )
    {
        return source.min( 3 ) <= t && t <= source.max( 3 );
    }

    @Override
    public RandomAccessibleInterval< T > getSource( final int t, final int level )
    {
        if ( t != currentTimePointIndex )
            loadTimepoint( t );
        return currentSource;
    }

    @Override
    public RealRandomAccessible< T > getInterpolatedSource( final int t, final int level, final Interpolation method )
    {
        if ( t != currentTimePointIndex )
            loadTimepoint( t );
        return currentInterpolatedSources[ method.ordinal() ];
    }

    @Override
    public synchronized void getSourceTransform( final int t, final int level, final AffineTransform3D transform )
    {
        transform.set( sourceTransform );
    }
}
