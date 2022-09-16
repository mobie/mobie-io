package dataformats;

import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import bdv.util.volatiles.SharedQueue;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.Dimensions;

@Slf4j
public abstract class BaseTest extends BaseSpimDataChecker {
    protected int expectedTimePoints = 0;
    protected int expectedChannelsNumber = 1;
    protected Dimensions expectedShape;
    protected String expectedDType;
    protected double[] expectedScale = null;
    protected String expectedUnit = null;


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
        Assertions.assertEquals(expectedShape, getShape());
        Assertions.assertEquals(expectedDType, getDType());
        if(expectedScale != null) {
            Assert.assertArrayEquals(expectedScale, getScale(), 0.0);
        }
        if(expectedUnit != null) {
            Assertions.assertEquals(expectedUnit, getUnit());
        }
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

    public Dimensions getExpectedShape() {
        return expectedShape;
    }

    public void setExpectedShape(Dimensions expectedShape) {
        this.expectedShape = expectedShape;
    }

    public String getExpectedDType() {
        return expectedDType;
    }

    public void setExpectedDType(String expectedDType) {
        this.expectedDType = expectedDType;
    }

    protected void setExpectedScale(double[] scale) {this.expectedScale = scale;}

    protected void setExpectedUnit(String unit) {this.expectedUnit = unit;}
}
