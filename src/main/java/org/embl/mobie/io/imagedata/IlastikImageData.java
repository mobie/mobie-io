package org.embl.mobie.io.imagedata;

import bdv.cache.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.Source;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.embl.mobie.io.util.IOHelper;
import org.embl.mobie.io.util.RandomAccessibleIntervalSource4D;
import org.janelia.saalfeldlab.n5.hdf5.N5HDF5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IlastikImageData< T extends NumericType< T > & NativeType< T >, V extends Volatile<T> & NumericType<V> > extends AbstractImageData< T >
{
    private static final String C = "c";
    private static final String Z = "z";
    private static final String T = "t";
    public static final String X = "x";
    public static final String Y = "y";
    private final String uri;
    private final SharedQueue sharedQueue;
    private boolean isOpen;
    private ArrayList< RandomAccessibleInterval< T > > channelRAIs;
    private ArrayList< RandomAccessibleInterval< V > > volatileChannelRAIs;

    public IlastikImageData( String uri, SharedQueue sharedQueue )
    {
        this.uri = uri;
        this.sharedQueue = sharedQueue;
    }

    @Override
    public Pair< Source< T >, Source< ? extends Volatile< T > > > getSourcePair( int datasetIndex )
    {
        if ( ! isOpen ) open();

        Source< T > source = asSource( channelRAIs.get( datasetIndex ) );
        Source< ? extends Volatile< T > > vSource = asVolatileSource( volatileChannelRAIs.get( datasetIndex ) );

        return new ValuePair<>( source, vSource );
    }

    private Source< T > asSource( RandomAccessibleInterval< T > rai )
    {
        if ( rai.numDimensions() == 3 )
        {
            // no time axis, thus we need to add one
            rai = Views.addDimension( rai, 0, 0 );
        }

        return new RandomAccessibleIntervalSource4D<>(
                rai,
                Util.getTypeFromInterval( rai ),
                new AffineTransform3D(),
                new FinalVoxelDimensions( "pixel", 1, 1, 1 ),
                "ilastik" );
    }

    private Source< V > asVolatileSource( RandomAccessibleInterval< V > rai )
    {
        if ( rai.numDimensions() == 3 )
        {
            // no time axis, thus we need to add one
            rai = Views.addDimension( rai, 0, 0 );
        }

        return new RandomAccessibleIntervalSource4D<>(
                rai,
                Util.getTypeFromInterval( rai ),
                new AffineTransform3D(),
                new FinalVoxelDimensions( "pixel", 1, 1, 1 ),
                "ilastik" );
    }


    @Override
    public int getNumDatasets()
    {
        if ( !isOpen ) open();

        return channelRAIs.size();
    }

    @Override
    public CanonicalDatasetMetadata getMetadata( int datasetIndex )
    {
        if ( !isOpen ) open();

        return null;
        // white
//        IntColorMetadata colorMetadata = new IntColorMetadata( ARGBType.rgba( 255, 255, 255, 255 ) );
//
//        // FIXME: in general, how do we deal with this being not known?
//        return new CanonicalDatasetMetadata(
//                uri,
//                null,
//                0,
//                1, // FIXME: what is correct?
//                colorMetadata
//        );
    }

    private synchronized void open()
    {
        if ( isOpen ) return;

        try
        {
            final N5HDF5Reader n5 = new N5HDF5Reader( uri );
            String dataset = "exported_data";
            if ( ! n5.datasetExists( dataset ) )
                dataset = "data";
            List< String > axes = fetchAxesLabels( n5, dataset );
            final CachedCellImg< T, ? > cachedCellImg = N5Utils.openVolatile( n5, dataset );
            channelRAIs = splitIntoChannels( cachedCellImg, axes );
            addNames( dataset );
            volatileChannelRAIs = ( ArrayList ) splitIntoChannels( getVolatileRAI( cachedCellImg ), axes );
            isOpen = true;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    private void addNames( String dataset )
    {
        int numChannels = channelRAIs.size();
        if ( numChannels > 1 )
        {
            for ( int channelIndex = 0; channelIndex < numChannels; channelIndex++ )
            {
                datasetNames.add( IOHelper.appendChannelPostfix( dataset, channelIndex ) );
            }
        }
        else
        {
            datasetNames.add( dataset );
        }
    }

    private RandomAccessibleInterval< ? extends Volatile< T > > getVolatileRAI( CachedCellImg< T, ? > cachedCellImg )
    {
        if ( sharedQueue == null )
        {
            return VolatileViews.wrapAsVolatile( cachedCellImg );
        }
        else
        {
            return VolatileViews.wrapAsVolatile( cachedCellImg, sharedQueue );
        }
    }

    private static List< String > fetchAxesLabels( N5HDF5Reader n5, String dataset ) throws IOException
    {
        try
        {
            ArrayList< String > axes = new ArrayList<>();
            final JsonObject axisTags = n5.getAttribute( dataset, "axistags", JsonObject.class );
            final JsonArray jsonArray = axisTags.get( "axes" ).getAsJsonArray();
            for ( JsonElement jsonElement : jsonArray )
            {
                final JsonObject jsonObject = jsonElement.getAsJsonObject();
                final String axisLabel = jsonObject.get( "key" ).getAsString();
                axes.add( axisLabel );
            }
            Collections.reverse( axes );
            return axes;
        }
        catch ( Exception e )
        {
            List< String > axes = Arrays.asList( T, Z, C, X, Y );
            Collections.reverse( axes );
            return axes;
        }
    }

    private static < T > ArrayList< RandomAccessibleInterval< T > > splitIntoChannels(
            final RandomAccessibleInterval< T > rai,
            List< String > axes )
    {
        if ( rai.numDimensions() != axes.size() )
            throw new IllegalArgumentException( "provided axes doesn't match dimensionality of image" );

        final ArrayList< RandomAccessibleInterval< T > > sourceStacks = new ArrayList<>();

        /*
         * If there is a channels dimension, slice img along that dimension.
         */
        final int c = axes.indexOf( C );
        if ( c != -1 )
        {
            final int numSlices = ( int ) rai.dimension( c );
            for ( int s = 0; s < numSlices; ++s )
                sourceStacks.add( Views.hyperSlice( rai, c, s + rai.min( c ) ) );
        } else
            sourceStacks.add( rai );

        /*
         * If AxisOrder is a 2D variant (has no Z dimension), augment the
         * sourceStacks by a Z dimension.
         */
        final boolean addZ = !axes.contains( Z );
        if ( addZ )
            sourceStacks.replaceAll( interval -> Views.addDimension( interval, 0, 0 ) );

        /*
         * If at this point the dim order is XYTZ, permute to XYZT
         */
        final boolean flipZ = !axes.contains( Z ) && axes.contains( T );
        if ( flipZ )
            sourceStacks.replaceAll( interval -> Views.permute( interval, 2, 3 ) );

        return sourceStacks;
    }
}
