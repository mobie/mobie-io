package develop;

import bdv.util.BdvFunctions;
import ij.ImagePlus;
import mpicbg.spim.data.generic.AbstractSpimData;
import org.embl.mobie.io.toml.TOMLOpener;

public class OpenTOML
{
	public static void main( String[] args )
	{
		final TOMLOpener opener = new TOMLOpener( "MVI_1235", "/Volumes/cba/exchange/kristina-mirkes/develop/data-test/processed/exp/batch/date/MVI_1253/exp--batch--date--mvi_1253.image.toml" );

		//final ImagePlus imagePlus = opener.asImagePlus();
		//imagePlus.show();

		final AbstractSpimData< ? > spimData = opener.asSpimData();
		BdvFunctions.show( spimData );
	}
}
