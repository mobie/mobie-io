/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2023 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package dataformats;

import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import bdv.cache.SharedQueue;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.Dimensions;


public abstract class BaseTest extends BaseSpimDataChecker {
    protected int expectedTimePoints = 0;
    protected int expectedChannelsNumber = 1;
    protected Dimensions expectedShape;
    protected String expectedDType;
    protected double[] expectedScale = null;
    protected String expectedUnit = null;

    protected BaseTest(String path, ImageDataFormat format) throws SpimDataException {
        super(new SpimDataOpener().open(path, format));
    }

    protected BaseTest(String path, ImageDataFormat format, SharedQueue sharedQueue) throws SpimDataException {
        super(new SpimDataOpener().open(path, format, sharedQueue));
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
