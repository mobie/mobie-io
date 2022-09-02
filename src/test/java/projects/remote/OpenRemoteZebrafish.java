/*-
 * #%L
 * Fiji viewer for MoBIE projects
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package projects.remote;

import org.embl.mobie.io.ImageDataFormat;
import org.embl.mobie.io.SpimDataOpener;

import bdv.util.volatiles.SharedQueue;
import lombok.extern.slf4j.Slf4j;
import mpicbg.spim.data.SpimDataException;

@Slf4j
public class OpenRemoteZebrafish extends BaseTest {
    //mpicbg.spim.data.SpimDataException: Error while trying to read spimDataorg.jdom2.input.JDOMParseException:
    // Error on line 16: Attribute name "crossorigin" associated with an element type "link" must be followed by the
    // ' = ' character.
    public OpenRemoteZebrafish() throws SpimDataException {
        super(new SpimDataOpener().openSpimData("https://raw.githubusercontent.com/mobie/zebrafish-lm-datasets/main/data/membrane/images/remote/membrane-056F63395C_lynEGFP.xml", ImageDataFormat.BdvN5S3, new SharedQueue(4)));
        setExpectedTimePoints(1);
    }
}
