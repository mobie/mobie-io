package projects;

import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimData;

@Slf4j
public abstract class BaseTest {
    protected SpimData spimData;

    protected int getTimePointsSize() {
        if (spimData == null) {
            log.warn("SpimData is null");
            return 0;
        }
        return spimData.getSequenceDescription().getTimePoints().size();
    }

    protected int getAllChannelsSize() {
        if (spimData == null) {
            log.warn("SpimData is null");
            return 0;
        }
        return spimData.getSequenceDescription().getAllChannels().size();
    }

    protected SpimData getSpimData() {
        return spimData;
    }

    protected void setSpimData(SpimData spimData) {
        this.spimData = spimData;
    }

    protected void baseTest(SpimData spimData){
        System.out.println(spimData.getSequenceDescription().getImgLoader().getClass().getName());
    }
}
/*
TODO: add tests for:
/Volumes/schwab/Karel/MOBIE/MOBIE1_bc"
/Volumes/cba/exchange/marianne-beckwidth/220509_MSB26_sample2_MoBIE".view("clem-registered"));
https://github.com/mobie/arabidopsis-root-lm-datasets
https://github.com/mobie/clem-example-project/ .view("Figure2a"));
https://github.com/mobie/covid-if-project .view("default"));
https://github.com/mobie/plankton-fibsem-project .dataset("micromonas"));
https://github.com/platybrowser/platybrowser");
https://github.com/mobie/platybrowser-datasets" .gitProjectBranch("normal-vie"));
 1) check the data format
 2) add similarly to the LocalAutophagosomesEMTest
 */
