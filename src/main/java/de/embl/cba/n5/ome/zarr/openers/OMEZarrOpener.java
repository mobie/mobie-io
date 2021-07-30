package de.embl.cba.n5.ome.zarr.openers;

import com.google.gson.GsonBuilder;
import de.embl.cba.n5.ome.zarr.loaders.N5OMEZarrImageLoader;
import de.embl.cba.n5.ome.zarr.readers.N5OmeZarrReader;
import de.embl.cba.n5.util.openers.BDVOpener;
import ij.IJ;
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
