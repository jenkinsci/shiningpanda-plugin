/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2012 ShiningPanda S.A.S.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jenkins.plugins.shiningpanda.utils;

import hudson.FilePath;
import hudson.Functions;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.StringUtils;

public class FilePathUtil
{

    /**
     * Join path
     * 
     * @param base
     *            The base path
     * @param parts
     *            The extra path parts
     * @return The joined path
     * @throws IOException
     * @throws InterruptedException
     */
    public static FilePath join(FilePath base, String... parts) throws IOException, InterruptedException
    {
        return new FilePath(base, StringUtils.join(parts, getSeparator(base)));
    }

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

    private static final class Separator implements Callable<String, IOException>
    {
        public String call() throws IOException
        {
            return File.separator;
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Get the separator on the OS hosting this file.
     * 
     * @param filePath
     *            The file
     * @return The separator
     * @throws IOException
     * @throws InterruptedException
     */
    public static String getSeparator(FilePath filePath) throws IOException, InterruptedException
    {
        return filePath.act(new Separator());
    }

    private static final class PathSeparator implements Callable<String, IOException>
    {
        public String call() throws IOException
        {
            return File.pathSeparator;
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Get the path separator on the OS hosting this file.
     * 
     * @param filePath
     *            The file
     * @return The path separator
     * @throws IOException
     * @throws InterruptedException
     */
    public static String getPathSeparator(FilePath filePath) throws IOException, InterruptedException
    {
        return filePath.act(new PathSeparator());
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
            if (filePath.exists())
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
            if (filePath.isDirectory())
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
            if (isFile(filePath))
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
        return filePath.exists() && !filePath.isDirectory();
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
        else if (src.isDirectory())
        {
            // Get the list of the files to synchronize
            List<FilePath> srcFiles = src.list(FileFileFilter.FILE);
            // Get the list of the related file names
            List<String> srcNames = new ArrayList<String>();
            for (FilePath srcFile : srcFiles)
                srcNames.add(srcFile.getName());
            // Delete files in destination folder that don't exist anymore in
            // source folder
            if (dest.exists())
                for (FilePath destFile : dest.list(FileFileFilter.FILE))
                    if (!srcNames.contains(destFile.getName()))
                        destFile.delete();
            // Synchronize all files
            for (FilePath srcFile : srcFiles)
                synchronize(srcFile, new FilePath(dest, srcFile.getName()));
        }
        // Return the destination file of directory
        return dest;
    }

}
