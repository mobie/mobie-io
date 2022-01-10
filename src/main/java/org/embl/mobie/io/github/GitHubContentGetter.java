/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package org.embl.mobie.io.github;

import com.drew.lang.annotations.Nullable;

public class GitHubContentGetter
{
	private String repository;
	private String path;
	private String branch;
	private String accessToken;

	/**
	 * https://developer.github.com/v3/repos/contents/
	 *
	 */
	public GitHubContentGetter(
			String repository,
			String path,
			@Nullable String branch,
			@Nullable String accessToken
	)
	{
		this.repository = repository;
		this.path = path;
		this.branch = branch;
		this.accessToken = accessToken;
	}

	public String getContent()
	{
		// GET /repos/:owner/:repo/contents/:path?ref=:branch

		String url = createGetContentApiUrl( path );
		final String requestMethod = "GET";
		final RESTCaller restCaller = new RESTCaller();
		return restCaller.get( url, requestMethod, accessToken );
	}

	public Integer getContentResponseCode() {
		// GET /repos/:owner/:repo/contents/:path?ref=:branch

		String url = createGetContentApiUrl( path );
		final String requestMethod = "GET";
		final RESTCaller restCaller = new RESTCaller();
		return restCaller.getResponseCode( url, requestMethod, accessToken );
	}

	private String createGetContentApiUrl( String path )
	{
		String url = repository.replace( "github.com", "api.github.com/repos" );
		if ( ! url.endsWith( "/" ) ) url += "/";
		if ( ! path.startsWith( "/" ) ) path = "/" + path;
		url += "contents" + path;
		if ( branch != null ) {
			if ( url.endsWith("/") ) {
				url = url.substring( 0, url.length() - 1 );
			}
			url += "?ref=" + branch;
		}
		return url;
	}

	public static void main( String[] args )
	{
		final GitHubContentGetter contentGetter = new GitHubContentGetter( "https://github.com/platybrowser/platybrowser", "data/1.0.1/misc/bookmarks" , "mobie", null );

		System.out.println( contentGetter.getContent() );
	}
}
