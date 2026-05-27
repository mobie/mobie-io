/*-
 * #%L
 * OME-Zarr extras for Fiji
 * %%
 * Copyright (C) 2022 - 2026 SciJava developers
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
package org.embl.mobie.io.zarrjava;

import com.google.gson.Gson;
import dev.zarr.zarrjava.experimental.ome.metadata.OmeroChannel;
import dev.zarr.zarrjava.experimental.ome.metadata.OmeroMetadata;
import dev.zarr.zarrjava.experimental.ome.metadata.OmeroRdefs;
import dev.zarr.zarrjava.experimental.ome.metadata.OmeroWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * The class is used to represent omero metadata.
 */
@SuppressWarnings( "all" )
public class Omero
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	// Top-level Omero class
	public int id;

	public String name;

	public Rdefs rdefs;

	public List< Channel > channels;

	public static class Rdefs
	{
		public int defaultT;

		public int defaultZ;

		public String model;
	}

	public static class Channel
	{
		public boolean active;

		public double coefficient;

		public String color;

		public String family;

		public boolean inverted;

		public String label;

		public Window window;

		public static class Window
		{
			public double start;

			public double end;

			public double min;

			public double max;
		}
	}

	@Override
	public String toString()
	{
		return new Gson().toJson( this );
	}

	public static String[] buildChannelLabels( final String fallbackName, final Omero omero, final int numChannels )
	{
		final boolean omeroValid = omero != null && omero.channels != null && omero.channels.size() == numChannels;
		if ( omeroValid )
			logger.trace( "Creating with OMERO metadata: {}", omero );
		else
			logger.trace( "Creating without OMERO metadata (not consistent or not available)" );

		final String[] labels = new String[ numChannels ];
		for ( int i = 0; i < numChannels; i++ )
			labels[ i ] = omeroValid ? omero.channels.get( i ).label : fallbackName;
		return labels;
	}

	public static Omero convertOmero( final OmeroMetadata source )
	{
		if ( source == null )
			return null;
		final Omero omero = new Omero();
		omero.id = source.id != null ? source.id : 0;
		omero.name = source.name;
		omero.rdefs = convertRdefs( source.rdefs );
		if ( source.channels != null )
		{
			final List< Omero.Channel > channels = new ArrayList<>( source.channels.size() );
			for ( final OmeroChannel channel : source.channels )
				channels.add( convertChannel( channel ) );
			omero.channels = channels;
		}
		return omero;
	}

	public static Omero.Rdefs convertRdefs( final OmeroRdefs source )
	{
		if ( source == null )
			return null;
		final Omero.Rdefs rdefs = new Omero.Rdefs();
		rdefs.defaultT = source.defaultT != null ? source.defaultT : 0;
		rdefs.defaultZ = source.defaultZ != null ? source.defaultZ : 0;
		rdefs.model = source.model;
		return rdefs;
	}

	public static Omero.Channel convertChannel( final OmeroChannel source )
	{
		if ( source == null )
			return null;
		final Omero.Channel channel = new Omero.Channel();
		channel.active = source.active != null && source.active;
		channel.coefficient = source.coefficient != null ? source.coefficient : 0.0;
		channel.color = source.color;
		channel.family = source.family;
		channel.inverted = source.inverted != null && source.inverted;
		channel.label = source.label;
		channel.window = convertWindow( source.window );
		return channel;
	}

	public static Omero.Channel.Window convertWindow( final OmeroWindow source )
	{
		if ( source == null )
			return null;
		final Omero.Channel.Window window = new Omero.Channel.Window();
		window.start = source.start != null ? source.start : 0.0;
		window.end = source.end != null ? source.end : 0.0;
		window.min = source.min != null ? source.min : 0.0;
		window.max = source.max != null ? source.max : 0.0;
		return window;
	}

}
