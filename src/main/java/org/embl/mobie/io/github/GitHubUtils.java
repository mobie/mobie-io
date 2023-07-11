/*-
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
package org.embl.mobie.io.github;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.embl.mobie.io.util.IOHelper;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import ij.gui.GenericDialog;

public abstract class GitHubUtils {
    public static String createRawUrl(String url) {
        return createRawUrl(url, null);
    }

    public static String createRawUrl(String url, String branch) {
        if (!url.contains("github.com")) {
            throw new UnsupportedOperationException("URL must contain github.com.");
        }

        String rawUrl = url.replace("github.com", "raw.githubusercontent.com");
        if (branch != null)
            if ( rawUrl.endsWith( "/" ) )
                rawUrl += branch;
            else
                rawUrl += "/" + branch;

        return rawUrl;
    }

    public static GitLocation rawUrlToGitLocation(String rawUrl) {
        final GitLocation gitLocation = new GitLocation();
        final String[] split = rawUrl.split("/");
        final String user = split[3];
        final String repo = split[4];
        gitLocation.branch = split[5];
        gitLocation.repoUrl = "https://github.com/" + user + "/" + repo;
        gitLocation.path = "";
        for (int i = 6; i < split.length; i++) {
            gitLocation.path += split[i] + "/";
        }
        return gitLocation;
    }

    // objectName is used for the dialog labels e.g. 'table', 'bookmark' etc...
    public static String selectGitHubPathFromDirectory(String directory, String objectName) throws IOException {
        final String[] fileNames = getFileNames(directory);

        final GenericDialog gd = new GenericDialog("Select " + objectName);
        gd.addChoice(objectName, fileNames, fileNames[0]);
        gd.showDialog();
        if (gd.wasCanceled()) return null;
        final String fileName = gd.getNextChoice();
        String newFilePath = IOHelper.combinePath(directory, fileName);

        return newFilePath;
    }

    public static String[] getFileNames(String directory) {
        final GitLocation gitLocation = GitHubUtils.rawUrlToGitLocation(directory);
        final ArrayList<String> filePaths = GitHubUtils.getFilePaths(gitLocation);
        return filePaths.stream().map(File::new).map(File::getName).toArray(String[]::new);
    }

    public static ArrayList<String> getFilePaths(GitLocation gitLocation) {
        final GitHubContentGetter contentGetter = new GitHubContentGetter(gitLocation.repoUrl, gitLocation.path, gitLocation.branch, null);
        final String json = contentGetter.getContent();

        GsonBuilder builder = new GsonBuilder();

        final ArrayList<String> bookmarkPaths = new ArrayList<>();
        ArrayList<LinkedTreeMap> linkedTreeMaps = (ArrayList<LinkedTreeMap>) builder.create().fromJson(json, Object.class);
        for (LinkedTreeMap linkedTreeMap : linkedTreeMaps) {
            final String downloadUrl = (String) linkedTreeMap.get("download_url");
            bookmarkPaths.add(downloadUrl);
        }
        return bookmarkPaths;
    }

    public static boolean isGithub(String directory) {
        return directory.contains("raw.githubusercontent");
    }
}
