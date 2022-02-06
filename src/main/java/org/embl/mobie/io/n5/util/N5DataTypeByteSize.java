package org.embl.mobie.io.n5.util;

import org.janelia.saalfeldlab.n5.DataType;

public abstract class N5DataTypeByteSize
{
	public static int getNumBytesPerElement( DataType dataType )
	{
		switch ( dataType )
		{
			case UINT8:
			case INT8:
				return 1;
			case UINT16:
			case INT16:
				return 2;
			case UINT32:
			case INT32:
			case FLOAT32:
				return 4;
			case UINT64:
			case INT64:
			case FLOAT64:
				return 8;
			case OBJECT:
			default:
				throw new UnsupportedOperationException( "Cannot determine the byte size of " + dataType );
		}
	}
}
