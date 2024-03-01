package org.embl.mobie.io.imagedata;

import bdv.viewer.Source;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import org.embl.mobie.io.metadata.ImageMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalSpatialDatasetMetadata;

public interface ImageData < T extends NumericType< T > & NativeType< T > >
{
    Pair< Source< T >, Source< ? extends Volatile< T > > > getSourcePair( int datasetIndex  );

    int getNumDatasets();

    //CanonicalSpatialDatasetMetadata getMetadata( int datasetIndex );
}