package projects.local;

import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import mpicbg.spim.data.SpimDataException;
import dataformats.BaseSpimDataChecker;

public class BaseLocalTest extends BaseSpimDataChecker {
    protected int expectedTimePoints = 0;
    protected int expectedChannelsNumber = 1;

    protected BaseLocalTest(String path, ImageDataFormat format) throws SpimDataException {
        super(new SpimDataOpener().openSpimData(path, format));
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
