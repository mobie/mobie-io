package org.embl.mobie.io.metadata;

import net.imglib2.type.numeric.ARGBType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;

public class ImagePlusImageMetadata implements ImageMetadata
{
    public ImagePlusImageMetadata()
    {

    }

    @Override
    public ARGBType getColor()
    {
        return null;
    }

    @Override
    public DatasetAttributes getAttributes()
    {
        return null;
    }

    @Override
    public String getPath()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return ImageMetadata.super.getName();
    }

}
