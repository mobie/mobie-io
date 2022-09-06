package spimdata;

import java.util.Arrays;

import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Info<N extends NumericType<N> & RealType<N>> {
    public int levels;
    public int level; // lowest resolution level
    double min = Double.MAX_VALUE; // at lowest resolution level
    double max = -Double.MAX_VALUE; // at lowest resolution level
    long[] dimensions; // at lowest resolution level

    public void print() {
        System.out.println("Levels: " + levels);
        System.out.println("Lowest level: " + level);
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("Dimensions: " + Arrays.toString(dimensions));
    }

    Info getImgInfo(SpimData spimData, int setupId) {
        // TODO we could add a method getNumTimepoints() to our ImageLoader?
        //   Then we could use this in the tests
        final MultiResolutionSetupImgLoader<N> setupImgLoader = (MultiResolutionSetupImgLoader) spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(setupId);
        final int numMipMapLevels = setupImgLoader.numMipmapLevels();
        final int level = numMipMapLevels - 1;

        final RandomAccessibleInterval<N> image = setupImgLoader.getImage(0, level);
        final Cursor<N> cursor = Views.iterable(image).cursor();
        final Info info = new Info();
        while (cursor.hasNext()) {
            final N next = cursor.next();
            if (next.getRealDouble() > info.max)
                info.max = next.getRealDouble();
            ;
            if (next.getRealDouble() < info.min)
                info.min = next.getRealDouble();
            ;
        }
        info.dimensions = image.dimensionsAsLongArray();
        info.level = level;
        info.levels = numMipMapLevels;

        return info;
    }
}
