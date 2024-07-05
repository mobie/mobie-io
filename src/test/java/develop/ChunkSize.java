package develop;

import bdv.export.ExportMipmapInfo;
import bdv.export.ProposeMipmaps;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import net.imglib2.FinalDimensions;

public class ChunkSize
{
    public static void main( String[] args )
    {
        // 2552x1658x706
        FinalDimensions finalDimensions = new FinalDimensions( 2552, 1658, 706 );
        FinalVoxelDimensions voxelDimensions = new FinalVoxelDimensions( "pixel", 1, 1, 1 );
        // propose reasonable mipmap settings
        final int maxNumElements = 64 * 64 * 64;
        final ExportMipmapInfo autoMipmapSettings = ProposeMipmaps.proposeMipmaps(
                new BasicViewSetup(0, "", finalDimensions, voxelDimensions),
                maxNumElements);

        int[][] resolutions = autoMipmapSettings.getExportResolutions();
        int[][] subdivisions = autoMipmapSettings.getSubdivisions();
    }
}
