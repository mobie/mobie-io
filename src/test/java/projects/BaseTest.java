package projects;

import org.junit.After;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimData;

@Slf4j
public abstract class BaseTest {
    SpimData spimData;

    protected int getTimePointsSize() {
        if (spimData == null) {
            log.warn("SpimData is null");
            return 0;
        }
        return spimData.getSequenceDescription().getTimePoints().size();
    }


    public SpimData getSpimData() {
        return spimData;
    }

    public void setSpimData(SpimData spimData) {
        this.spimData = spimData;
    }
//
//    @Test
//    @Order(1)
//    public void tryTest(){
//        System.out.println(spimData.getSequenceDescription());
//        log.debug("BASE TEST");
//    }
}
