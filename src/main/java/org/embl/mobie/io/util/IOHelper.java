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
package org.embl.mobie.io.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;

import ij.ImagePlus;
import ij.io.Opener;
import loci.common.ByteArrayHandle;
import loci.common.DebugTools;
import loci.common.Location;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import org.apache.commons.io.IOUtils;
import org.embl.mobie.io.github.GitHubUtils;

import com.amazonaws.services.s3.AmazonS3;
import org.jetbrains.annotations.NotNull;

import static org.embl.mobie.io.github.GitHubUtils.isGithub;
import static org.embl.mobie.io.github.GitHubUtils.selectGitHubPathFromDirectory;
import static org.embl.mobie.io.util.S3Utils.getS3FileNames;
import static org.embl.mobie.io.util.S3Utils.selectS3PathFromDirectory;

public class IOHelper {

    static {
        DebugTools.setRootLevel( "OFF" ); // Disable Bio-Formats logging
    }

    public static ResourceType getType(String uri)
    {
        if ( ( uri.startsWith("http") && uri.contains( "s3." ) ) )
        {
            return IOHelper.ResourceType.S3;
        }
        else if ( uri.startsWith("http") && uri.contains( ".zarr" ) )
        {
            return IOHelper.ResourceType.S3;
        }
        else if ( uri.startsWith("http") )
        {
            return IOHelper.ResourceType.HTTP;
        }
        else
        {
            return IOHelper.ResourceType.FILE;
        }
    }

    public static BufferedReader getReader(String path) {
        InputStream stream;
        try {
            stream = getInputStream(path);
        } catch (IOException e) {
            throw new RuntimeException("Could not open " + path);
        }
        final InputStreamReader inReader = new InputStreamReader(stream);
        final BufferedReader bufferedReader = new BufferedReader(inReader);
        return bufferedReader;
    }

    public static List<File> getFileList(File directory, String fileNameRegExp, boolean recursive) {
        final ArrayList<File> files = new ArrayList<>();
        populateFileList(directory, fileNameRegExp, files, recursive);
        return files;
    }

    public static void populateFileList(File directory, String fileNameRegExp, List<File> files, boolean recursive) {

        // Get all the files from a directory.
        File[] fList = directory.listFiles();

        if (fList != null) {
            for (File file : fList) {
                if (file.isFile()) {
                    final Matcher matcher = Pattern.compile(fileNameRegExp).matcher(file.getName());
                    if (matcher.matches())
                        files.add(file);
                } else if (file.isDirectory() && recursive) {
                    populateFileList(file, fileNameRegExp, files, recursive);
                }
            }
        }
    }

    public static List<String> getFiles(File inputDirectory, String filePattern) {
        final List<File> fileList = getFileList(inputDirectory, filePattern, false);

        Collections.sort(fileList, new IOHelper.SortFilesIgnoreCase());

        final List<String> paths = fileList.stream().map(x -> x.toString()).collect(Collectors.toList());

        return paths;
    }

    public static ImagePlus openWithBioFormats( String path, int seriesIndex )
    {
        ResourceType type = getType( path );

        switch ( type )
        {
            case S3:
                return openWithBioFormatsFromS3( path, seriesIndex );
            case FILE:
                return openWithBioFormatsFromFile( path, seriesIndex );
            default:
                throw new RuntimeException( "Cannot open " + path + " with BioFormats." );
        }
    }

    public static ImagePlus openWithBioFormatsFromFile( String path, int seriesIndex )
    {
        try
        {
            ImporterOptions opts = new ImporterOptions();
            opts.setId( path );
            opts.setVirtual( true );
            opts.setSeriesOn( seriesIndex, true );
            ImportProcess process = new ImportProcess( opts );
            process.execute();
            ImagePlusReader impReader = new ImagePlusReader( process );
            ImagePlus[] imps = impReader.openImagePlus();
            return imps[ 0 ];
        }
        catch ( Exception e )
        {
            throw new RuntimeException("Could not open " + path );
        }
    }

    public static String getSeparator(String uri) {
        IOHelper.ResourceType type = getType(uri);
        switch (type) {
            case FILE:
                return File.separator;
            case HTTP:
            case S3:
            default:
                return "/";
        }
    }

    public static String combinePath(String... paths) {
        final String separator = getSeparator(paths[0]);

        String combined = paths[0];
        for (int i = 1; i < paths.length; i++) {
            if (combined.endsWith(separator))
                combined = combined + paths[i];
            else
                combined = combined + separator + paths[i];
        }

        return combined;
    }

    public static String removeTrailingSlash(String path) {
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return path;
    }

    public static InputStream getInputStream(String uri) throws IOException {
        IOHelper.ResourceType type = getType(uri);
        switch (type) {
            case HTTP:
                URL url = new URL(uri);
                return url.openStream();
            case FILE:
                return Files.newInputStream( new File( uri ).toPath() );
            case S3:
                AmazonS3 s3 = S3Utils.getS3Client( uri );
                String[] bucketAndObject = S3Utils.getBucketAndObject(uri);
                return s3.getObject(bucketAndObject[0], bucketAndObject[1]).getObjectContent();
            default:
                throw new IOException("Could not open uri: " + uri);
        }
    }

    public static String read(String uri) throws IOException {
        try (final InputStream inputStream = IOHelper.getInputStream(uri)) {
            final String s = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            return s;
        }
    }

    // overwrites existing uri
    public static void write(String uri, String text) throws IOException {
        IOHelper.ResourceType type = getType(uri);
        switch (type) {
            case FILE:
                writeFile(uri, text);
                return;
            case S3:
                writeS3Object(uri, text);
                return;
            case HTTP:
            default:
                throw new IOException("Could not save to URI: " + uri);
        }
    }

    // overwrite existing object
    public static void writeS3Object(String uri, String text) {
        AmazonS3 s3 = S3Utils.getS3Client(uri);
        String[] bucketAndObject = S3Utils.getBucketAndObject(uri);
        s3.putObject(bucketAndObject[0], bucketAndObject[1], text);
    }

    // overwrites existing file
    public static void writeFile(String path, String text) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(text);
        writer.close();
    }

    public static String getParentLocation(String uri) {
        IOHelper.ResourceType type = getType(uri);
        switch (type) {
            case HTTP:
            case S3:
                return getHttpParentLocation(uri);
            case FILE:
                return new File(uri).getParent();
            default:
                throw new RuntimeException("Invalid uri: " + uri);
        }
    }

    public static String getFileName( String uri) {
        IOHelper.ResourceType type = getType(uri);
        switch (type) {
            case HTTP:
            case S3:
                final String[] split = uri.split( "/" );
                return split[ split.length - 1 ];
            case FILE:
                return new File(uri).getName();
            default:
                throw new RuntimeException("Invalid uri: " + uri);
        }
    }


    public static String getHttpParentLocation(String uri) {
        try {
            URI uri1 = new URI(uri);
            URI parent = uri1.getPath().endsWith("/") ? uri1.resolve("..") : uri1.resolve(".");
            return parent.toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL Syntax: " + uri);
        }
    }

    public static void openURI(String uri) {
        try {
            java.awt.Desktop.getDesktop().browse(new URI(uri));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static boolean exists(String uri) {
        IOHelper.ResourceType type = getType(uri);
        switch (type) {
            case HTTP:
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(uri).openConnection();
                    con.setRequestMethod("HEAD");
                    return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            case FILE:
                return new File(uri).exists();
            case S3:
                AmazonS3 s3 = S3Utils.getS3Client(uri);
                String[] bucketAndObject = S3Utils.getBucketAndObject(uri);
                return s3.doesObjectExist(bucketAndObject[0], bucketAndObject[1]);
            default:
                return false;
        }
    }

    public static String[] getFileNames(String uri) {
        if (uri == null) {
            return null;
        }

        IOHelper.ResourceType type = getType(uri);
        switch (type) {
            case HTTP:
                if (isGithub(uri)) {
                    return GitHubUtils.getFileNames(uri);
                } else {
                    // TODO - implement for other kinds of http?
                    return null;
                }
            case FILE:
                List<File> files = getFileList(new File(uri), ".*", false);
                if ( !files.isEmpty() ) {
                    String[] fileNames = new String[files.size()];
                    for (int i = 0; i < files.size(); i++) {
                        fileNames[i] = files.get(i).getName();
                    }
                    return fileNames;
                } else {
                    return null;
                }
            case S3:
                return getS3FileNames(uri);
            default:
                return null;
        }
    }

    // objectName is used for the dialog labels e.g. 'table' etc...
    public static String selectPath(String uri, String objectName) throws IOException {

        if (uri == null) {
            return null;
        }

        IOHelper.ResourceType type = getType(uri);
        String filePath = null;
        switch (type) {
            case HTTP:
                if (isGithub(uri)) {
                    filePath = selectGitHubPathFromDirectory(uri, objectName);
                } else {
                    // TODO - implement for other kinds of http?
                    filePath = null;
                }
                break;
            case FILE:
                final JFileChooser jFileChooser = new JFileChooser(uri);
                jFileChooser.setDialogTitle("Select " + objectName);
                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                    filePath = jFileChooser.getSelectedFile().getAbsolutePath();
                break;
            case S3:
                filePath = selectS3PathFromDirectory(uri, objectName);
                break;
            default:
                return null;
        }

        if (filePath == null) return null;

        if (filePath.startsWith("http"))
            filePath = resolveURL(URI.create(filePath));

        return filePath;

    }

    public static String resolveURL(URI uri) {
        while (isRelativePath(uri.toString())) {
            URI relativeURI = URI.create(getRelativePath(uri.toString()));
            uri = uri.resolve(relativeURI).normalize();
        }

        return uri.toString();
    }

    /**
     * The path points to a file that contains itself a path (e.g. MoBIE tables).
     *
     * @param path
     * @return
     */
    public static String resolvePath(String path) {
        try {
            while (isRelativePath(path)) {
                String relativePath = getRelativePath(path);
                path = new File(new File(path).getParent(), relativePath).getCanonicalPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }

    /**
     * Checks whether the file contains a path, pointing to another
     * version of itself.
     *
     * @param path
     * @return
     */
    public static boolean isRelativePath(String path) {
        try (final BufferedReader reader = getReader(path)) {
            final String firstLine = reader.readLine();
            return firstLine.startsWith("..");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getRelativePath(String path) {
        try (final BufferedReader reader = getReader(path)) {
            String link = reader.readLine();
            return link;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean stringContainsItemFromList(String inputStr, ArrayList<String> items) {
        return items.parallelStream().anyMatch(inputStr::contains);
    }

	public static ImagePlus openTiffFromFile( String imagePath )
	{
		final File file = new File( imagePath );
		final ImagePlus imagePlus = (new Opener()).openTiff( file.getParent(), file.getName() );
		return imagePlus;
	}

    public static List< String > getNamedGroups( String regex )
    {
        List< String > namedGroups = new ArrayList<>();

        Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher( regex );

        while ( m.find() ) {
            namedGroups.add(m.group(1));
        }

        return namedGroups;
    }

    public static List< String > getPaths( String regex, int maxDepth )
    {
        final String dir = new File( regex ).getParent();
        String name = new File( regex ).getName();
        return getPaths( dir, name, maxDepth );
    }

    public static List< String > getPaths( String dir, String regex, int maxDepth )
    {
        try
        {
            final List< String > paths = Files.find( Paths.get( dir ), maxDepth,
                            ( path, basicFileAttribute ) -> {
                                final boolean isFileOrDirectory = basicFileAttribute.isRegularFile() || basicFileAttribute.isDirectory();
                                final String fileName = path.getFileName().toString();
                                final boolean matchesRegex = fileName.matches( regex );
                                return isFileOrDirectory && matchesRegex;
                            } )
                    .map( path -> path.toString() ).collect( Collectors.toList() );
            Collections.sort( paths );

            if ( paths.size() == 0 )
            {
                System.err.println("Could not find any files matching " + regex + " within " + dir );
            }

            return paths;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }

    public static ImagePlus openWithBioFormatsFromS3( String path, int seriesIndex )
    {
        try
        {
            InputStream inputStream = getInputStream( path );
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[ 1024 ];
            while ( ( nRead = inputStream.read( data, 0, data.length ) ) != -1 )
                buffer.write( data, 0, nRead );
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            //System.out.println( byteArray.length + " bytes read from S3." );
            Location.mapFile( "mapped_" + path, new ByteArrayHandle( byteArray ) );
            //System.out.println( "S3 [ms]: " + ( System.currentTimeMillis() - start ) );
            return openWithBioFormatsFromFile( "mapped_" + path, seriesIndex );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    @NotNull
    public static String addChannelPostfix( String name, int channelIndex )
    {
        if ( name.endsWith( "_" ) )
            return name + getChannelPostfix( channelIndex );
        else
            return name + "_" + getChannelPostfix( channelIndex );
    }

    @NotNull
    public static String getChannelPostfix( int channelIndex )
    {
        return "ch" + channelIndex;
    }


    public enum ResourceType {
        FILE,  // resource is a file on the file system
        HTTP,  // resource supports http requests
        S3     // resource supports s3 API
    }

    public static class SortFilesIgnoreCase implements Comparator<File> {
        public int compare(File o1, File o2) {
            String s1 = o1.getName();
            String s2 = o2.getName();
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }

}
