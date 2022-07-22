/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2015 ShiningPanda S.A.S.
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
import hudson.Util;
import hudson.remoting.Callable;
import jenkins.plugins.shiningpanda.interpreters.Python;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FilePathUtil {

    private static final class IsWindows implements Callable<Boolean, IOException> {
        public Boolean call() throws IOException {
            return Functions.isWindows();
        }

        private static final long serialVersionUID = 1L;

        @Override
        public void checkRoles(RoleChecker arg0) throws SecurityException {
            // nothing to do
        }
    }

    public static boolean isWindows(FilePath filePath) throws IOException, InterruptedException {
        return filePath.act(new IsWindows()).booleanValue();
    }

    public static boolean isUnix(FilePath filePath) throws IOException, InterruptedException {
        return !isWindows(filePath);
    }

    public static final class Read implements Callable<String, IOException> {
        private String file;
        private String encoding;

        public Read(FilePath filePath, String encoding) {
            // Call super
            super();
            // Store the file path
            this.file = filePath.getRemote();
            // Store encoding
            this.encoding = encoding;
        }

        public String call() throws IOException {
            // Read content
            return FileUtils.readFileToString(new File(file), encoding);
        }

        @Override
        public void checkRoles(RoleChecker arg0) throws SecurityException {
            // nothing to do
        }

        private static final long serialVersionUID = 1L;
    }

    public static String read(FilePath filePath, String encoding) throws IOException, InterruptedException {
        // Sometimes FilePath.readToString doesn't work, don't ask me why...
        return filePath.act(new Read(filePath, encoding));
    }

    public static FilePath existsOrNull(FilePath... filePaths) throws IOException, InterruptedException {
        for (FilePath filePath : filePaths)
            if (filePath != null && filePath.exists())
                return filePath;
        return null;
    }

    public static FilePath isDirectoryOrNull(FilePath... filePaths) throws IOException, InterruptedException {
        for (FilePath filePath : filePaths)
            if (filePath != null && filePath.isDirectory())
                return filePath;
        return null;
    }

    public static FilePath isFileOrNull(FilePath... filePaths) throws IOException, InterruptedException {
        for (FilePath filePath : filePaths)
            if (filePath != null && isFile(filePath))
                return filePath;
        return null;
    }

    public static boolean differ(FilePath filePath1, FilePath filePath2) throws IOException, InterruptedException {
        if (filePath1.exists() && filePath2.exists())
            return filePath1.digest() != filePath2.digest();
        return true;
    }

    public static boolean differ(FilePath filePath, String content) throws IOException, InterruptedException {
        if (filePath.exists())
            return filePath.digest() != Util.getDigestOf(content);
        return true;
    }

    public static boolean isFile(FilePath filePath) throws IOException, InterruptedException {
        return filePath != null && filePath.exists() && !filePath.isDirectory();
    }

    public static boolean isDirectory(FilePath filePath) throws IOException, InterruptedException {
        return filePath != null && filePath.isDirectory();
    }

    public static FilePath synchronize(FilePath filePath, String content) throws IOException, InterruptedException {
        // Check if differ
        if (differ(filePath, content))
            // If differ, write
            filePath.write(content, "UTF-8");
        // Return the file path
        return filePath;
    }

    public static FilePath synchronize(FilePath src, FilePath dest) throws IOException, InterruptedException {
        // Handle files
        if (isFile(src)) {
            // Check if differ
            if (differ(src, dest))
                // If differ, copy
                src.copyTo(dest);
        }
        // Handle directory
        else if (isDirectory(src)) {
            // Get the list of the files to synchronize
            List<FilePath> srcFiles = src.list(FileFileFilter.INSTANCE);
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
                for (FilePath destFile : dest.list(FileFileFilter.INSTANCE))
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

    public static List<FilePath> listSharedLibraries(Python interpreter) throws IOException, InterruptedException {
        return listSharedLibraries(interpreter.getHome().child("lib"));
    }

    public static List<FilePath> listSharedLibraries(FilePath filePath) throws IOException, InterruptedException {
        return !isDirectory(filePath) ? Collections.<FilePath>emptyList()
                : Arrays.asList(filePath.list("*.so,*.so.*,*.dylib,*.dylib.*"));
    }

}
