package org.embl.mobie.io.util;

import bdv.ViewerImgLoader;
import bdv.cache.SharedQueue;
import bdv.img.cache.VolatileGlobalCellCache;
import ch.epfl.biop.bdv.img.CacheControlOverride;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;

public class SharedQueueHelper
{
    public static void setSharedQueue( SharedQueue sharedQueue, AbstractSpimData< ? > spimData )
    {
        BasicImgLoader imgLoader = spimData.getSequenceDescription().getImgLoader();

        if ( imgLoader instanceof CacheControlOverride )
        {
            CacheControlOverride cco = ( CacheControlOverride ) imgLoader;
            final VolatileGlobalCellCache volatileGlobalCellCache = new VolatileGlobalCellCache( sharedQueue );
            cco.setCacheControl( volatileGlobalCellCache );
        }
        else if ( imgLoader instanceof ViewerImgLoader )
        {
            ( ( ViewerImgLoader ) imgLoader ).setCreatedSharedQueue( sharedQueue );
        }
        else
        {
            // cannot set the sharedQueue
        }
    }
}
