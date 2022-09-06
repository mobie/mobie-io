package projects.remote;

import java.util.ArrayList;

import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import bdv.util.volatiles.SharedQueue;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.Dimensions;
import projects.BaseSpimDataChecker;

@Slf4j
public abstract class BaseTest extends BaseSpimDataChecker {
    protected int expectedTimePoints = 0;
    protected int expectedChannelsNumber = 1;
    protected Dimensions expectedShape;
    protected String expectedDType;
    protected ArrayList<Integer[]> coordinates;
    protected ArrayList<Integer> expectedValues;

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
        checkExpectedValues();
    }
    
    protected void checkExpectedValues() {
        final int nValues = this.expectedValues.size();
        Assertions.assertEquals(this.coordinates.size(), nValues);
        
        for(int i = 0; i < nValues; i++) {
            int expectedValue = this.expectedValues.get(i);
            Integer[] coordinates = this.coordinates.get(i); 
            // TODO set timepoint and channel here
            RandomAccessibleInterval<?> randomAccessibleInterval = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0);
        // UnsignedShortType o = (UnsignedShortType)  randomAccessibleInterval.getAt(0, 0, 0);
        }
        // int value = o.get();
        // Assertions.assertEquals(538, value);
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

    public void setCoordinates(ArrayList<Integer[]> coordinates) {
        this.coordinates = coordinates;
    }

    public ArrayList<Integer[]> getCoordinates() {
        return this.coordinates;
    }
    
    public void setExpectedValues(ArrayList<Integer> expectedValues) {
        this.expectedValues = expectedValues;
    }

    public ArrayList<Integer> getExpectedValues() {
        return this.expectedValues;
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
