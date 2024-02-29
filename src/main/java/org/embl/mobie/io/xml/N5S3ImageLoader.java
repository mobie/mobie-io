/*
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2023 EMBL
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
package org.embl.mobie.io.xml;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.cache.SharedQueue;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.viewer.SourceAndConverter;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.MultiResolutionImgLoader;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.queue.FetcherThreads;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.embl.mobie.io.imagedata.N5ImageData;
import org.embl.mobie.io.util.IOHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


public class N5S3ImageLoader< T extends NumericType< T > & NativeType< T > >  implements ViewerImgLoader, MultiResolutionImgLoader {

    private final Map<Integer, SetupImgLoader > setupImgLoaders = new HashMap<>();
    private final String serviceEndpoint;
    private final String signingRegion;
    private final String bucketName;
    private final String key;
    private AbstractSequenceDescription<?, ?, ?> seq;
    private ViewRegistrations viewRegistrations;
    private volatile boolean isOpen = false;
    private FetcherThreads fetchers;
    private VolatileGlobalCellCache cache;
    private BlockingFetchQueues<Callable<?>> queue;

    private SharedQueue sharedQueue = null;


    public N5S3ImageLoader( String serviceEndpoint, String signingRegion, String bucketName, String key, AbstractSequenceDescription< ?, ?, ? > seq )
    {
        this.serviceEndpoint = serviceEndpoint;
        this.signingRegion = signingRegion;
        this.bucketName = bucketName;
        this.key = key;
        this.seq = seq;
    }

    @Override
    public SetupImgLoader getSetupImgLoader( final int setupId )
    {
        open();
        return setupImgLoaders.get( setupId );
    }

    private void open()
    {
        if ( !isOpen )
        {
            synchronized ( this )
            {
                if ( isOpen )
                    return;

                String uri = IOHelper.combinePath( serviceEndpoint, bucketName, key );
                N5ImageData< T > imageData = new N5ImageData<>( uri, sharedQueue );

                List< SourceAndConverter< T > > sourcesAndConverters = imageData.getSourcesAndConverters();
                int numSetups = sourcesAndConverters.size();
                for ( int setupId = 0; setupId < numSetups; setupId++ )
                {
                    SetupImgLoader setupImgLoader = new SetupImgLoader( sourcesAndConverters.get( setupId ) );
                    setupImgLoaders.put( setupId, setupImgLoader );
                   // maxNumLevels = Math.max( maxNumLevels, setupImgLoader.numMipmapLevels() );
                }
            }
        }
    }

    @Override
    public CacheControl getCacheControl()
    {
        return null;
    }

    @Override
    public void setNumFetcherThreads( int n )
    {
        ViewerImgLoader.super.setNumFetcherThreads( n );
    }

    @Override
    public void setCreatedSharedQueue( SharedQueue createdSharedQueue )
    {
        ViewerImgLoader.super.setCreatedSharedQueue( createdSharedQueue );
    }

    public String getServiceEndpoint()
    {
        return serviceEndpoint;
    }

    public String getSigningRegion()
    {
        return signingRegion;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public String getKey()
    {
        return key;
    }

    public class SetupImgLoader< T extends NativeType< T >, V extends Volatile< T > & NativeType< V > >
            extends AbstractViewerSetupImgLoader< T, V >
            implements MultiResolutionSetupImgLoader< T >
    {
        private final SourceAndConverter< T > sourceAndConverter;

        public SetupImgLoader( final SourceAndConverter< T > sourceAndConverter )
        {
            super( sourceAndConverter.getSpimSource().getType(), ( V ) sourceAndConverter.asVolatile().getSpimSource().getType() );
            this.sourceAndConverter = sourceAndConverter;
        }

        @Override
        public RandomAccessibleInterval< V > getVolatileImage( final int timepointId, final int level, final ImgLoaderHint... hints )
        {
            return ( RandomAccessibleInterval< V > ) sourceAndConverter.asVolatile().getSpimSource().getSource( timepointId, level );
        }

        @Override
        public RandomAccessibleInterval< T > getImage( final int timepointId, final int level, final ImgLoaderHint... hints )
        {
            return sourceAndConverter.getSpimSource().getSource( timepointId, level );
        }

        @Override
        public Dimensions getImageSize( final int timepointId, final int level )
        {
            return null;
        }

        @Override
        public double[][] getMipmapResolutions()
        {
            return null;
        }

        @Override
        public AffineTransform3D[] getMipmapTransforms()
        {
            return null;
        }

        @Override
        public int numMipmapLevels()
        {
            return -1;
        }

        @Override
        public VoxelDimensions getVoxelSize( final int timepointId )
        {
            return null;
        }

    }

}
