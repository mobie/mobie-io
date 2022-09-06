package org.embl.mobie.io.n5.openers;

public abstract class BDVOpener {
    protected static boolean logging;

    public static void setLogging(final boolean logging) {
        S3Opener.logging = logging;
    }

}
