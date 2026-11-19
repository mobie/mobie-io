package org.embl.mobie.io.imagedata;

import bdv.viewer.SourceAndConverter;
import net.imglib2.display.ColorConverter;
import ome.zarr.fiji.bdv.OmeroDisplay;
import ome.zarr.imglib2.PyramidContents;
import ome.zarr.imglib2.metadata.Omero;
import org.embl.mobie.io.util.IOHelper;
import org.janelia.saalfeldlab.n5.universe.metadata.IntColorMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the {@link Omero} metadata of an OME-Zarr into what {@link ImageData} hands to MoBIE:
 * one name and one {@link CanonicalDatasetMetadata} per channel.
 */
final class OmeroMetadata
{
    private OmeroMetadata()
    {}

    /**
     * The OMERO channel label of each channel. A channel without one is named like
     * {@link N5ImageData} names it: the file name, with a channel postfix if there
     * is more than one channel.
     */
    static List< String > channelNames( String uri, PyramidContents< ? > contents, int numChannels )
    {
        String fileName = IOHelper.getFileName( uri );
        List< Omero.Channel > omeroChannels = OmeroDisplay.channels( contents.omero(), numChannels );
        boolean noChannels = omeroChannels.isEmpty();
        List< String > names = new ArrayList<>();
        for ( int channelIndex = 0; channelIndex < numChannels; channelIndex++ )
        {
            String label = noChannels ? null : omeroChannels.get( channelIndex ).label();
            if ( label != null && !label.isEmpty() )
                names.add( label );
            else if ( numChannels > 1 )
                names.add( IOHelper.appendChannelPostfix( fileName, channelIndex ) );
            else
                names.add( fileName );
        }
        return names;
    }

    /**
     * The color and contrast limits of each source, taken from the {@link Omero} channel
     * {@code color} and {@code window} {@code start}/{@code end} where the metadata
     * has them, and from the source's converter otherwise.
     * An entry is {@code null} if the converter is not a {@link ColorConverter}, which
     * does not happen for the RealType sources of a PyramidalBdv.
     */
    static List< CanonicalDatasetMetadata > channelMetadata( String uri, PyramidContents< ? > contents,
            List< ? extends SourceAndConverter< ? > > sources )
    {
        List< Omero.Channel > omeroChannels = OmeroDisplay.channels( contents.omero(), sources.size() );
        boolean noChannels = omeroChannels.isEmpty();
        List< CanonicalDatasetMetadata > metadata = new ArrayList<>();
        for ( int channelIndex = 0; channelIndex < sources.size(); channelIndex++ )
        {
            Object converter = sources.get( channelIndex ).getConverter();
            if ( !( converter instanceof ColorConverter ) )
            {
                metadata.add( null );
                continue;
            }
            ColorConverter colorConverter = ( ColorConverter ) converter;
            double min = colorConverter.getMin();
            double max = colorConverter.getMax();
            int color = colorConverter.getColor().get();

            Omero.Channel omeroChannel = noChannels ? null : omeroChannels.get( channelIndex );
            if ( omeroChannel != null && omeroChannel.window() != null )
            {
                min = omeroChannel.window().start();
                max = omeroChannel.window().end();
            }
            if ( omeroChannel != null && omeroChannel.color() != null )
                color = 0xff000000 | Integer.parseInt( omeroChannel.color(), 16 );

            metadata.add( new CanonicalDatasetMetadata( uri, null, min, max, new IntColorMetadata( color ) ) );
        }
        return metadata;
    }
}
