/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2022 EMBL
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

public class GitHubFileCommitter {
    private final String repository;
    private final String accessToken;
    private final String path;
    private final String branch;
    private final String sha; // needed if updating an existing file

    public GitHubFileCommitter(String repository, String accessToken, String path) {
        this(repository, accessToken, null, path, null);
    }

    public GitHubFileCommitter(String repository, String accessToken, String branch, String path) {
        this(repository, accessToken, branch, path, null);
    }

    public GitHubFileCommitter(String repository, String accessToken, String branch, String path,
                               String sha) {
        this.repository = repository;
        this.accessToken = accessToken;
        this.path = path;
        this.branch = branch;
        this.sha = sha;
    }

    public void commitStringAsFile(String message, String base64String) {
        final GitHubFileCommit fileCommit = new GitHubFileCommit(message, base64String, branch, sha);
        String url = createFileCommitApiUrl(path);
        final String requestMethod = "PUT";
        final String json = fileCommit.toString();

        new RESTCaller().put(url, requestMethod, json, accessToken);
    }

    public String createFileCommitApiUrl(String path) {
        String url = repository.replace("github.com", "api.github.com/repos");
        if (!url.endsWith("/")) url += "/";
        if (!path.startsWith("/")) path = "/" + path;
        url += "contents" + path;
        return url;
    }
}
