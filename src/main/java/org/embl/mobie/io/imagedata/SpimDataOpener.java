package org.embl.mobie.io.imagedata;

import mpicbg.spim.data.generic.AbstractSpimData;

import java.io.IOException;

public interface SpimDataOpener
{
    AbstractSpimData open( String uri ) throws Exception;
}
