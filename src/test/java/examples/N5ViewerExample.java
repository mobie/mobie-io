package examples;

import org.janelia.saalfeldlab.n5.bdv.N5Viewer;

public class N5ViewerExample
{
    public static void main( String[] args )
    {
        String uri = "/Users/tischer/Downloads/20240524_1_s2.zarr";
        N5Viewer.show( uri );
    }
}
