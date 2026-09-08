package org.embl.mobie.io;

import org.scijava.Context;

public class ContextProvider
{
    public static Context getContext()
    {
        return context;
    }

    public static void setContext( Context context )
    {
        ContextProvider.context = context;
    }

    public static Context context = new Context();
}
