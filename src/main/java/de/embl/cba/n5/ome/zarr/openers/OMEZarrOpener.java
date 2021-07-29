package de.embl.cba.n5.ome.zarr.openers;

import com.google.gson.GsonBuilder;

import de.embl.cba.n5.ome.zarr.readers.N5OmeZarrReader;
import ij.IJ;
import mpicbg.spim.data.SpimData;
import net.imglib2.util.Cast;

import java.io.File;
import java.io.IOException;
import de.embl.cba.n5.ome.zarr.loaders.N5OMEZarrImageLoader;

public class OMEZarrOpener
{
    private static boolean logChunkLoading;

    private final String filePath;

    public OMEZarrOpener(String filePath)
    {
        this.filePath = filePath;
    }

    public static void setLogChunkLoading( boolean logChunkLoading )
    {
        OMEZarrOpener.logChunkLoading = logChunkLoading;
        if ( logChunkLoading ) IJ.run("Console");
    }

    public static SpimData openFile(String filePath) throws IOException
    {
        setLogChunkLoading(true);
        N5OMEZarrImageLoader.logChunkLoading = logChunkLoading;
        OMEZarrOpener omeZarrOpener = new OMEZarrOpener(filePath);
        return omeZarrOpener.readFile();
    }

    private SpimData readFile() throws IOException
    {
        N5OMEZarrImageLoader.logChunkLoading = logChunkLoading;
        N5OmeZarrReader reader = new N5OmeZarrReader(this.filePath, new GsonBuilder());
        N5OMEZarrImageLoader imageLoader = new N5OMEZarrImageLoader(reader);
        return new SpimData(
                new File(this.filePath),
                Cast.unchecked( imageLoader.getSequenceDescription() ),
                imageLoader.getViewRegistrations());
    }

}
