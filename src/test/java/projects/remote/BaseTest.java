package projects.remote;

import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import bdv.util.volatiles.SharedQueue;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import projects.BaseSpimDataChecker;

@Slf4j
public abstract class BaseTest extends BaseSpimDataChecker {
    protected int expectedTimePoints = 0;
    protected int expectedChannelsNumber = 1;

    protected BaseTest(String path, ImageDataFormat format) throws SpimDataException {
        super(new SpimDataOpener().openSpimData(path, format));
    }

    protected BaseTest(String path, ImageDataFormat format, SharedQueue sharedQueue) throws SpimDataException {
        super(new SpimDataOpener().openSpimData(path, format, sharedQueue));
    }

    @Test
    public void baseTest() {
        Assertions.assertEquals(expectedTimePoints, getTimePointsSize());
        Assertions.assertEquals(expectedChannelsNumber, getAllChannelsSize());
    }

    public int getExpectedTimePoints() {
        return expectedTimePoints;
    }

    public void setExpectedTimePoints(int expectedTimePoints) {
        this.expectedTimePoints = expectedTimePoints;
    }

    public int getExpectedChannelsNumber() {
        return expectedChannelsNumber;
    }

    public void setExpectedChannelsNumber(int expectedChannelsNumber) {
        this.expectedChannelsNumber = expectedChannelsNumber;
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
 2) add similarly to the AutophagosomesEMTest
 */
