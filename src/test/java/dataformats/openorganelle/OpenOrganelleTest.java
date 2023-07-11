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
package dataformats.openorganelle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.embl.mobie.io.ImageDataFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;


import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalDimensions;
import dataformats.BaseTest;


public class OpenOrganelleTest extends BaseTest {
    private static final String URL = "https://janelia-cosem.s3.amazonaws.com/jrc_hela-2/jrc_hela-2.n5/em/fibsem-uint16";
    private static final ImageDataFormat FORMAT = ImageDataFormat.OpenOrganelleS3;
    public static final int N = 3;
    public final Map<long[], Object> trueValuesMap = new LinkedHashMap<>();

    public OpenOrganelleTest() throws SpimDataException {
        super(URL, FORMAT);
        setExpectedTimePoints(1);
        setExpectedShape(new FinalDimensions(12000, 1600, 6368));
        setExpectedDType("uint16");
        init();
    }

    public int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public void init() {
        System.out.println("Before init() method called");
        long[] imageDimensions = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).dimensionsAsLongArray();
        for (int i = 0; i <= N; i++) {
            long x = getRandomNumberUsingNextInt(0, Math.toIntExact(imageDimensions[0] - 1));
            long y = getRandomNumberUsingNextInt(0, Math.toIntExact(imageDimensions[1] - 1));
            long z = getRandomNumberUsingNextInt(0, Math.toIntExact(imageDimensions[2] - 1));
            long[] axes = new long[]{x, y, z};
            Object realPixelValue = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).getAt(x, y, z);
            trueValuesMap.put(axes, realPixelValue);
        }
    }

    @Test
    @DisplayName("Random SpimData test")
    public void RandomSpimDataTest() {
        System.out.println("Running random test");
        List<Object> testValues = new ArrayList<>();
        for (long[] axes : trueValuesMap.keySet()) {
            Object realPixelValue = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).getAt(axes[0], axes[1], axes[2]);
            testValues.add(realPixelValue);
        }
        assertArrayEquals(testValues.toArray(), trueValuesMap.values().toArray());
    }
}
