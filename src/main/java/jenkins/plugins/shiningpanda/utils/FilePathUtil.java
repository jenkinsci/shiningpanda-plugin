/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2013 ShiningPanda S.A.S.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of its license which incorporates the terms and 
 * conditions of version 3 of the GNU Affero General Public License, 
 * supplemented by the additional permissions under the GNU Affero GPL
 * version 3 section 7: if you modify this program, or any covered work, 
 * by linking or combining it with other code, such other code is not 
 * for that reason alone subject to any of the requirements of the GNU
 * Affero GPL version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * license for more details.
 *
 * You should have received a copy of the license along with this program.
 * If not, see <https://raw.github.com/jenkinsci/shiningpanda-plugin/master/LICENSE.txt>.
 */
package jenkins.plugins.shiningpanda.utils;

import hudson.FilePath;
import hudson.Functions;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jenkins.plugins.shiningpanda.interpreters.Python;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;

public class FilePathUtil
{

    private static final class IsWindows implements Callable<Boolean, IOException>
    {
        public Boolean call() throws IOException
        {
            return Functions.isWindows();
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Is this file located on Windows?
     * 
     * @param filePath
     *            The file to check
     * @return true if the file is on Windows, else false
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean isWindows(FilePath filePath) throws IOException, InterruptedException
    {
        return filePath.act(new IsWindows()).booleanValue();
    }

    /**
     * Is this file located on UNIX?
     * 
     * @param filePath
     *            The file to check
     * @return true if the file is on UNIX, else false
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean isUnix(FilePath filePath) throws IOException, InterruptedException
    {
        return !isWindows(filePath);
    }

    /**
     * Get a file contents.
     */
    public static final class Read implements Callable<String, IOException>
    {
        /**
         * Store the path.
         */
        private String file;

        /**
         * Store the encoding.
         */
        private String encoding;

        /**
         * Constructor using fields.
         * 
         * @param filePath
         *            The file path
         * @param encoding
         *            The encoding
         */
        public Read(FilePath filePath, String encoding)
        {
            // Call super
            super();
            // Store the file path
            this.file = filePath.getRemote();
            // Store encoding
            this.encoding = encoding;
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.remoting.Callable#call()
         */
        public String call() throws IOException
        {
            // Read content
            return FileUtils.readFileToString(new File(file), encoding);
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Get the content of the provided file path.
     * 
     * @param filePath
     *            The file path
     * @param encoding
     *            The encoding
     * @return The content of the file
     * @throws IOException
     * @throws InterruptedException
     */
    public static String read(FilePath filePath, String encoding) throws IOException, InterruptedException
    {
        // Sometimes FilePath.readToString doesn't work, don't ask me why...
        return filePath.act(new Read(filePath, encoding));
    }

    /**
     * Get the first existing file or directory.
     * 
     * @param filePaths
     *            The list of files or directories to check
     * @return The first existing file or directory, else null
     * @throws IOException
     * @throws InterruptedException
     */
    public static FilePath existsOrNull(FilePath... filePaths) throws IOException, InterruptedException
    {
        for (FilePath filePath : filePaths)
            if (filePath != null && filePath.exists())
                return filePath;
        return null;
    }

    /**
     * Get the first existing directory.
     * 
     * @param filePaths
     *            The list of directories to check
     * @return The first existing directory, else null
     * @throws IOException
     * @throws InterruptedException
     */
    public static FilePath isDirectoryOrNull(FilePath... filePaths) throws IOException, InterruptedException
    {
        for (FilePath filePath : filePaths)
            if (filePath != null && filePath.isDirectory())
                return filePath;
        return null;
    }

    /**
     * Get the first existing file.
     * 
     * @param filePaths
     *            The list of files to check
     * @return The first existing file, else null
     * @throws IOException
     * @throws InterruptedException
     */
    public static FilePath isFileOrNull(FilePath... filePaths) throws IOException, InterruptedException
    {
        for (FilePath filePath : filePaths)
            if (filePath != null && isFile(filePath))
                return filePath;
        return null;
    }

    /**
     * Check if two files are equal.
     * 
     * @param filePath1
     *            The first file
     * @param filePath2
     *            The second file
     * @return true if the files are equal, false if files differ
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean differ(FilePath filePath1, FilePath filePath2) throws IOException, InterruptedException
    {
        if (filePath1.exists() && filePath2.exists())
            return filePath1.digest() != filePath2.digest();
        return true;
    }

    /**
     * Check if the provided path is a file
     * 
     * @param filePath
     *            The file to check
     * @return true if this is a file, else false
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean isFile(FilePath filePath) throws IOException, InterruptedException
    {
        return filePath != null && filePath.exists() && !filePath.isDirectory();
    }

    /**
     * Check if the provided path is a directory
     * 
     * @param filePath
     *            The path to check
     * @return true if this is a directory, else false
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean isDirectory(FilePath filePath) throws IOException, InterruptedException
    {
        return filePath != null && filePath.isDirectory();
    }

    /**
     * Synchronize a file or all files in a directory.
     * 
     * @param src
     *            The source file or directory
     * @param dest
     *            The destination file or directory
     * @return The destination file or directory
     * @throws IOException
     * @throws InterruptedException
     */
    public static FilePath synchronize(FilePath src, FilePath dest) throws IOException, InterruptedException
    {
        // Handle files
        if (isFile(src))
        {
            // Check if differ
            if (differ(src, dest))
                // If differ, copy
                src.copyTo(dest);
        }
        // Handle directory
        else if (isDirectory(src))
        {
            // Get the list of the files to synchronize
            List<FilePath> srcFiles = src.list(FileFileFilter.FILE);
            // Get the list of the related file names
            List<String> srcNames = new ArrayList<String>();
            // Go threw the files
            for (FilePath srcFile : srcFiles)
                // Add to the source list
                srcNames.add(srcFile.getName());
            // Delete files in destination folder that don't exist anymore in
            // source folder
            if (dest.exists())
                // List the files
                for (FilePath destFile : dest.list(FileFileFilter.FILE))
                    // Check if contained in the source files
                    if (!srcNames.contains(destFile.getName()))
                        // If not delete it
                        destFile.delete();
            // Synchronize all files
            for (FilePath srcFile : srcFiles)
                // Synchronize folders
                synchronize(srcFile, new FilePath(dest, srcFile.getName()));
        }
        // Return the destination file of directory
        return dest;
    }

    /**
     * List the shared libraries of the provided interpreter.
     * 
     * @param interpreter
     *            The interpreter
     * @return The list of shared libraries
     * @throws InterruptedException
     * @throws IOException
     */
    public static List<FilePath> listSharedLibraries(Python interpreter) throws IOException, InterruptedException
    {
        return listSharedLibraries(interpreter.getHome().child("lib"));
    }

    /**
     * List shared libraries contained in this folder.
     * 
     * @param filePath
     *            The folder
     * @return The list of libraries
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<FilePath> listSharedLibraries(FilePath filePath) throws IOException, InterruptedException
    {
        return !isDirectory(filePath) ? Collections.<FilePath> emptyList() : Arrays.asList(filePath
                .list("*.so,*.so.*,*.dylib,*.dylib.*"));
    }

}
