package org.embl.mobie.io.ome.zarr.hackathon.parsers;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.embl.mobie.io.ome.zarr.hackathon.Multiscales;
import org.embl.mobie.io.ome.zarr.hackathon.Multiscales.CoordinateTransformations;
import org.embl.mobie.io.ome.zarr.hackathon.Multiscales.Dataset;
import org.embl.mobie.io.ome.zarr.hackathon.Multiscales.Axis;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5DatasetDiscoverer;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5TreeNode;
import org.janelia.saalfeldlab.n5.RawCompression;
import org.janelia.saalfeldlab.n5.metadata.N5MetadataParser;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;

public class OmeZarrMultiscaleMetadataParser implements N5MetadataParser<Multiscales>
{

	@Override
	public Optional<Multiscales> parseMetadata(N5Reader n5, N5TreeNode node)
	{
		try {
			final Multiscales ms = n5.getAttribute(node.getPath(), "multiscales", Multiscales.class);

			if (ms == null)
				return Optional.empty();

			ms.path = node.getPath();

			// note this parser is responsible for adding child nodes
			// TODO perform checks here for safety
			for (Dataset d : ms.getDatasets()) {
				N5TreeNode child = new N5TreeNode(d.path);
				child.setMetadata(d);
				node.add(child);
			}

			return Optional.of(ms);

		} catch (IOException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	public static void main( String[] args ) throws IOException
	{
		final String rootPath = args[0];

		// make a 2d zarr
		final N5ZarrWriter zarr = new N5ZarrWriter(rootPath);
		
		final CoordinateTransformations cs = new CoordinateTransformations();
		cs.type = "scale";
		cs.scale = new double[]{ 1, 1 };

		Dataset dataset = new Dataset();
		dataset.coordinateTransformations = new CoordinateTransformations[] { cs };
		dataset.path = "0";

		Axis[] axes = new Axis[] {
				new Axis( "x", "space", "micrometers"),
				new Axis( "y", "space", "micrometers") };

		Multiscales ms = new Multiscales("0.4", "test", "", 
				axes, 
				new Dataset[] { dataset }, 
				new CoordinateTransformations[] { });
		ms.path = "";

		zarr.createGroup("/ms");
		zarr.createDataset("/ms/0", new DatasetAttributes( new long[]{5,5},  new int[]{5,5}, DataType.UINT8, new RawCompression()));
		zarr.setAttribute("/ms", "multiscales", ms );

		// try to parse the metadata
		OmeZarrMultiscaleMetadataParser parser = new OmeZarrMultiscaleMetadataParser();
		Optional<Multiscales> meta = parser.parseMetadata(zarr, "ms");
		System.out.println( meta ); // metadata should be not null, if so parser works
		System.out.println( "" );

		// if parser works, automated discovery should work too
		N5DatasetDiscoverer disc = new N5DatasetDiscoverer(zarr, Collections.emptyList() ,Collections.singletonList( parser ));
		N5TreeNode tree = disc.discoverAndParseRecursive("");
		tree.printRecursive();
	}

}
