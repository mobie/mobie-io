package develop;

import bdv.cache.SharedQueue;
import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.ImageDataOpener;
import org.embl.mobie.io.imagedata.ImageData;

public class OpenOpenOrganelleN5
{
    public static void main( String[] args )
    {
        ImageData< ? > imageData = ImageDataOpener.open(
                "s3://janelia-cosem-datasets/jrc_macrophage-2/jrc_macrophage-2.n5/em/fibsem-uint16",
                ImageDataFormat.fromPath( "s3://janelia-cosem-datasets/jrc_macrophage-2/jrc_macrophage-2.n5/em/fibsem-uint16" ),
                new SharedQueue( Runtime.getRuntime().availableProcessors() - 1 )
        );
        System.out.println( imageData.getNumDatasets() );
    }
}
