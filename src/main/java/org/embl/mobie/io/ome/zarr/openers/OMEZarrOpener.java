package org.embl.mobie.io.ome.zarr.openers;

import bdv.util.volatiles.SharedQueue;
import com.google.gson.GsonBuilder;
import org.embl.mobie.io.ome.zarr.loaders.N5OMEZarrImageLoader;
import org.embl.mobie.io.ome.zarr.readers.N5OmeZarrReader;
import org.embl.mobie.io.util.openers.BDVOpener;
import mpicbg.spim.data.SpimData;
import net.imglib2.util.Cast;

import java.io.File;
import java.io.IOException;

public class OMEZarrOpener extends BDVOpener {
    private final String filePath;

    public OMEZarrOpener(String filePath) {
        this.filePath = filePath;
    }

    public static SpimData openFile(String filePath) throws IOException {
        OMEZarrOpener omeZarrOpener = new OMEZarrOpener(filePath);
        return omeZarrOpener.readFile();
    }
    public static SpimData openFile(String filePath, SharedQueue sharedQueue) throws IOException
    {
        N5OMEZarrImageLoader.logChunkLoading = logChunkLoading;
        OMEZarrOpener omeZarrOpener = new OMEZarrOpener(filePath);
        return omeZarrOpener.readFile(sharedQueue);
    }

    private SpimData readFile(SharedQueue sharedQueue) throws IOException
    {
        N5OMEZarrImageLoader.logChunkLoading = logChunkLoading;
        N5OmeZarrReader reader = new N5OmeZarrReader(this.filePath, new GsonBuilder());
        N5OMEZarrImageLoader imageLoader = new N5OMEZarrImageLoader(reader, sharedQueue);
        return new SpimData(
                new File(this.filePath),
                Cast.unchecked( imageLoader.getSequenceDescription() ),
                imageLoader.getViewRegistrations());
    }

    private SpimData readFile() throws IOException {
        N5OMEZarrImageLoader.logChunkLoading = logChunkLoading;
        N5OmeZarrReader reader = new N5OmeZarrReader(this.filePath, new GsonBuilder());
        N5OMEZarrImageLoader imageLoader = new N5OMEZarrImageLoader(reader);
        return new SpimData(
                new File(this.filePath),
                Cast.unchecked(imageLoader.getSequenceDescription()),
                imageLoader.getViewRegistrations());
    }

}
