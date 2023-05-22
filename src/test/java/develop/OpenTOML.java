package develop;

import org.embl.mobie.io.util.TOMLOpener;

public class OpenTOML
{
	public static void main( String[] args )
	{
		final TOMLOpener opener = new TOMLOpener( "/Volumes/cba/exchange/kristina-mirkes/develop/data-test/processed/exp/batch/date/MVI_1253/exp--batch--date--mvi_1253.image.toml" );

		opener.asImagePlus();
	}
}
