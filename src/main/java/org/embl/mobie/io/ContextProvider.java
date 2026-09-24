package org.embl.mobie.io;

import org.scijava.Context;

public class ContextProvider
{
    private static volatile Context context;

    public static Context getContext()
    {
        if ( context == null )
        {
            synchronized ( ContextProvider.class )
            {
                if ( context == null )
                {
                    try
                    {
                        context = new Context();
                    }
                    catch ( Throwable t )
                    {
                        throw new IllegalStateException(
                                "Could not initialize SciJava Context. " +
                                        "In tests, initialize the ImageJ legacy layer (e.g. LegacyInjector.preinit()) " +
                                        "or inject a context via ContextProvider.setContext(...).",
                                t );
                    }
                }
            }
        }

        return context;
    }

    public static void setContext( Context context )
    {
        ContextProvider.context = context;
    }
}
