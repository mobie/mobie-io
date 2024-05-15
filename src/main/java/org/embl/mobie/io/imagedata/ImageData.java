package org.embl.mobie.io.imagedata;

import bdv.viewer.Source;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalDatasetMetadata;

public interface ImageData < T extends NumericType< T > & NativeType< T > >
{
    Pair< Source< T >, Source< ? extends Volatile< T > > > getSourcePair( int datasetIndex  );

    int getNumDatasets();

    // https://imagesc.zulipchat.com/#narrow/stream/327326-BigDataViewer/topic/Dataset.20Metadata
    CanonicalDatasetMetadata getMetadata( int datasetIndex );
}
