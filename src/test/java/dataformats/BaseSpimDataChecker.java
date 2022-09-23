package dataformats;

import mpicbg.spim.data.sequence.ViewId;
import mpicbg.spim.data.sequence.VoxelDimensions;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.sequence.ImgLoader;
import net.imglib2.Dimensions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Cast;

@Slf4j
public class BaseSpimDataChecker {
    protected final SpimData spimData;

    public BaseSpimDataChecker(AbstractSpimData spimData) throws ClassCastException {
        this.spimData = (SpimData) spimData;
    }

    protected SpimData getSpimData() {
        return spimData;
    }

    protected int getAllChannelsSize() {
        if (spimData == null) {
            log.warn("SpimData is null");
            return 0;
        }
        return spimData.getSequenceDescription().getAllChannels().size();
    }

    protected int getTimePointsSize() {
        if (spimData == null) {
            log.warn("SpimData is null");
            return 0;
        }
        return spimData.getSequenceDescription().getTimePoints().size();
    }

    protected Dimensions getShape() {
        if (spimData == null) {
            log.warn("SpimData is null");
            return null;
        }
        return spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImageSize(0);
    }

    protected String getDType() {
        if (spimData == null) {
            log.warn("SpimData is null");
            return null;
        }

        ImgLoader imageLoader = spimData.getSequenceDescription().getImgLoader();
        final Object type = imageLoader.getSetupImgLoader(0).getImageType();
        if (type instanceof RealType &&
            type instanceof NativeType &&
            N5Utils.dataType(Cast.unchecked(type)) != null) {
            return N5Utils.dataType(Cast.unchecked(type)).toString();
        }
        return "";
    }

    protected double[] getScale() {
        final double[] scale = new double[3];
        spimData.getSequenceDescription().getViewSetupsOrdered().get(0).getVoxelSize().dimensions(scale);
        return scale;
    }

    protected String getUnit() {
        VoxelDimensions voxelDimensions = spimData.getSequenceDescription().getViewDescription(new ViewId(0,0)).getViewSetup().getVoxelSize();
        String unit = voxelDimensions.unit();
        return unit;
    }

}
