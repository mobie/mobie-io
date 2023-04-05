/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2022 EMBL
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
package ui;

import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;

import bdv.util.BdvFunctions;
import bdv.cache.SharedQueue;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;

public class BdvOmeZarrOpener {
    public static void main(String[] args) {
        showProject();
    }

    public static void showProject() {
        SharedQueue sharedQueue = new SharedQueue(7);
        SpimDataOpener spimDataOpener = new SpimDataOpener();
        SpimData image = null;
        try {
//            image =(SpimData) spimDataOpener.openSpimData("https://raw.githubusercontent.com/mobie/clem-example-project//more-views/data/hela/images/bdv-n5-s3/fluorescence-a2-FMR-c2.xml",
//                    ImageDataFormat.BdvN5S3, sharedQueue);
            image = (SpimData) spimDataOpener.open("https://s3.embl.de/i2k-2020/project-bdv-ome-zarr/Covid19-S4-Area2/images/bdv.ome.zarr.s3/raw.xml",
                ImageDataFormat.BdvOmeZarrS3, sharedQueue);
        } catch (SpimDataException e) {
            e.printStackTrace();
        }
        assert image != null;
        BdvFunctions.show(image);
    }
}
