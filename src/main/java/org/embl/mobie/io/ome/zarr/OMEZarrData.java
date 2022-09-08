/*-
 * #%L
 * Expose the Imaris XT interface as an ImageJ2 service backed by ImgLib2.
 * %%
 * Copyright (C) 2019 - 2021 Bitplane AG
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.io.ome.zarr;

import Imaris.IDataSetPrx;
import bdv.util.volatiles.SharedQueue;
import com.bitplane.xt.util.ColorTableUtils;
import org.scijava.Context;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class OMEZarrData
{
	private final Context context;
	private final String omeZarrPath;
	private String[] imagePyramidPaths;
	private Map< String, ZarrImagePyramid< ?, ? > > imagePyramids;

	OMEZarrData(
			final Context context,
			final String omeZarrPath,
			@Nullable final SharedQueue queue  )
	{
		this.context = context;
		this.omeZarrPath = omeZarrPath;
		this.imagePyramidPaths = fetchImagePyramidPaths( omeZarrPath );
		this.imagePyramids = new HashMap<>();
		for ( String zArrayPath : imagePyramidPaths )
			imagePyramids.put( zArrayPath, new ZarrImagePyramid( zArrayPath, queue, false ) );
		// TODO we could fetch some other (collection) metadata, once specified
	}

	private String[] fetchImagePyramidPaths( String omeZarrPath )
	{
		// TODO
		return new String[ 0 ];
	}

	public OMEZarrDataset getDataset()
	{
		final String firstPyramidName = imagePyramids.keySet().iterator().next();
		return new SinglePyramidOMEZarrDataset<>(
				context,
				firstPyramidName,
				imagePyramids.get( firstPyramidName ) );
	}

	// TODO: add methods for creating a dataset by
	//  combining and/or slicing one or several imagePyramids

	public Set< String > getPyramidNames()
	{
		return imagePyramids.keySet();
	}
}
