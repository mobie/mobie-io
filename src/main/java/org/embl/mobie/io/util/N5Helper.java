package org.embl.mobie.io.util;

import com.amazonaws.auth.BasicAWSCredentials;
import org.janelia.saalfeldlab.n5.universe.N5Factory;

public abstract class N5Helper
{
    public static N5Factory n5Factory()
    {
        String[] s3AccessAndSecretKey = S3Utils.getS3AccessAndSecretKey();
        if( s3AccessAndSecretKey != null )
        {
            BasicAWSCredentials credentials = new BasicAWSCredentials( s3AccessAndSecretKey[ 0 ], s3AccessAndSecretKey[ 1 ] );
            return new N5Factory().s3UseCredentials( credentials );
        }
        else
        {
            return new N5Factory();
        }
    }
}
