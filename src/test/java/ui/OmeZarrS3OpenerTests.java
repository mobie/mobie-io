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

import java.io.IOException;
import java.util.List;

import org.embl.mobie.io.ome.zarr.loaders.N5OMEZarrImageLoader;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import mpicbg.spim.data.SpimData;
import net.imglib2.type.numeric.ARGBType;

import static org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener.readURL;

public class OmeZarrS3OpenerTests {

    public static void main(String[] args) throws IOException {
        //showMyosin();
        //showAll();
        //readI2KGif();
        //showIDR0();
        showIDR1();
    }

    public static void showIDR0() throws IOException {
        //  /idr/zarr/v0.1/6001237.zarr
        N5OMEZarrImageLoader.logging = true;
        OMEZarrS3Opener reader = new OMEZarrS3Opener("https://s3.embassy.ebi.ac.uk", "us-west-2", "idr");
        SpimData image = readURL("zarr/v0.1/6001237.zarr");
        List<BdvStackSource<?>> sources = BdvFunctions.show(image);
        sources.get(0).setColor(new ARGBType(ARGBType.rgba(0, 0, 255, 255)));
        sources.get(0).setDisplayRange(0, 3000);
        sources.get(1).setColor(new ARGBType(ARGBType.rgba(0, 255, 0, 255)));
        sources.get(1).setDisplayRange(0, 3000);
        sources.get(2).setColor(new ARGBType(ARGBType.rgba(255, 0, 0, 255)));
        sources.get(2).setDisplayRange(0, 3000);
        sources.get(3).setColor(new ARGBType(ARGBType.rgba(255, 255, 255, 255)));
        sources.get(3).setDisplayRange(0, 3000);
        //sources.get( 4 ).setDisplayRange( 0, 100 );
        // Sources.showAsLabelMask(sources.get(4));
    }

    public static void readI2KGif() throws IOException {
        // https://play.minio.io:9000/i2k2020/gif.zarr
        N5OMEZarrImageLoader.logging = true;
        OMEZarrS3Opener reader = new OMEZarrS3Opener("https://play.minio.io:9000", "us-west-2", "i2k2020");
        SpimData image = readURL("gif.zarr");
        BdvFunctions.show(image);
    }

    public static void showAll() throws IOException {
        N5OMEZarrImageLoader.logging = true;
        OMEZarrS3Opener reader = new OMEZarrS3Opener("https://s3.embl.de", "us-west-2", "i2k-2020");
        SpimData myosin = readURL("prospr-myosin.ome.zarr");
        List<BdvStackSource<?>> myosinBdvSources = BdvFunctions.show(myosin);
        SpimData em = readURL("em-raw.ome.zarr");
        List<BdvStackSource<?>> sources = BdvFunctions.show(em, BdvOptions.options().addTo(myosinBdvSources.get(0).getBdvHandle()));
        // Sources.showAsLabelMask(sources.get(1));
        // Sources.viewAsHyperstack(sources.get(0), 4);
    }

    public static void showMyosin() throws IOException {
        N5OMEZarrImageLoader.logging = true;
        OMEZarrS3Opener reader = new OMEZarrS3Opener("https://s3.embl.de", "us-west-2", "i2k-2020");
        SpimData myosin = readURL("prospr-myosin.ome.zarr");
        BdvFunctions.show(myosin);
    }

    public static void showIDR1() throws IOException {
        N5OMEZarrImageLoader.logging = true;
        OMEZarrS3Opener reader = new OMEZarrS3Opener("https://s3.embassy.ebi.ac.uk", "us-west-2", "idr");
        SpimData data = readURL("zarr/v0.1/9822151.zarr");
        BdvFunctions.show(data, BdvOptions.options().is2D()).get(0).setDisplayRange(3000, 15000);
    }
}
