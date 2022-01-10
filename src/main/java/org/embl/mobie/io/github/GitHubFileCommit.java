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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

@JsonInclude(JsonInclude.Include.NON_NULL) // exclude all null fields from toString()
public class GitHubFileCommit
{
	public String message = "My commit message";
	public String content = Base64.getEncoder().encodeToString( "Hello World".getBytes() );;
	public String branch;
	public String sha; // Needed if updating existing file

	public GitHubFileCommit( String message, String base64String )
	{
		this(message, base64String, null, null);
	}

	public GitHubFileCommit( String message, String base64String, String branch )
	{
		this(message, base64String, branch, null);
	}

	public GitHubFileCommit( String message, String base64String, String branch, String sha)
	{
		this.message = message;
		content = base64String;
		this.branch = branch;
		this.sha = sha;
	}

	@Override
	public String toString()
	{
		final ObjectMapper objectMapper = new ObjectMapper();
		try
		{
			return objectMapper.writeValueAsString( this );
		} catch ( JsonProcessingException e )
		{
			e.printStackTrace();
			throw new RuntimeException( "Could not build Json string" );
		}
	}
}
