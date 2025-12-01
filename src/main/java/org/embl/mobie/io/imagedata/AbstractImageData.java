package org.embl.mobie.io.imagedata;

import bdv.viewer.Source;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;

import java.util.ArrayList;
import java.util.List;

public class AbstractImageData< T extends NumericType< T > & NativeType< T > > implements ImageData< T >
{
    List< String > datasetNames = new ArrayList<>();
    List< CanonicalDatasetMetadata > metadata = new ArrayList<>();

    @Override
    public Pair< Source< T >, Source< ? extends Volatile< T > > > getSourcePair( int datasetIndex )
    {
        return null;
    }

    @Override
    public int getNumDatasets()
    {
        return datasetNames.size();
    }

    @Override
    public CanonicalDatasetMetadata getMetadata( int datasetIndex )
    {
        return null;
    }

    @Override
    public String getName( int datasetIndex )
    {
        return datasetNames.get( datasetIndex );
    }
}
