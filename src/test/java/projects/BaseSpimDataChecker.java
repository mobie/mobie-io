package projects;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.generic.AbstractSpimData;

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
}
