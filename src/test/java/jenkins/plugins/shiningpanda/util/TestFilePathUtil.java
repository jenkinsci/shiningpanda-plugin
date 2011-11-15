package jenkins.plugins.shiningpanda.util;

import hudson.FilePath;

import java.io.File;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;

import org.apache.commons.io.FileUtils;

public class TestFilePathUtil extends ShiningPandaTestCase
{

    public void testJoin() throws Exception
    {
        assertEquals("toto" + File.separator + "tata" + File.separator + "tutu",
                FilePathUtil.join(getFilePath("toto"), "tata", "tutu").getRemote());
    }

    public void testIsWindows() throws Exception
    {
        assertFalse("this is not a Windows", FilePathUtil.isWindows(getFilePath("toto")));
    }

    public void testIsUnix() throws Exception
    {
        assertTrue("this should be an UNIX", FilePathUtil.isUnix(getFilePath("toto")));
    }

    public void testGetSeparator() throws Exception
    {
        assertEquals("invalid separator", File.separator, FilePathUtil.getSeparator(getFilePath("toto")));
    }

    public void testGetPathSeparator() throws Exception
    {
        assertEquals("invalid path separator", File.pathSeparator, FilePathUtil.getPathSeparator(getFilePath("toto")));
    }

    public void testExistsOrNullNotExists() throws Exception
    {
        assertNull("file does not exist, this should return null", FilePathUtil.existsOrNull(getFilePath("quinexistepas")));
    }

    public void testExistsOrNullExists() throws Exception
    {
        File file = new File(createTempDir(), "file.txt");
        FileUtils.writeStringToFile(file, "hello");
        FilePath filePath = FilePathUtil.existsOrNull(new FilePath(file));
        assertNotNull("file exists, this should not return null", filePath);
    }

    public void testExistsOrNullSecondExists() throws Exception
    {
        File file = createTempDir();
        FilePath filePath = FilePathUtil.existsOrNull(getFilePath("quinexistepas"), new FilePath(file));
        assertNotNull("second file exists, this should not return null", filePath);
        assertEquals(file.getAbsolutePath(), filePath.getRemote());
    }

    public void testIsFileOrNullNotExists() throws Exception
    {
        assertNull("file does not exist, this should return null", FilePathUtil.existsOrNull(getFilePath("quinexistepas")));
    }

    public void testIsFileOrNullExistsButDirectory() throws Exception
    {
        assertNull("file exists but is a directory, this should return null",
                FilePathUtil.isFileOrNull(new FilePath(createTempDir())));
    }

    public void testIsFileOrNullExists() throws Exception
    {
        File file = new File(createTempDir(), "file.txt");
        FileUtils.writeStringToFile(file, "hello");
        FilePath filePath = FilePathUtil.isFileOrNull(new FilePath(file));
        assertNotNull("file exists, this should not return null", filePath);
    }

    public void testIsFileOrNullSecondIsFile() throws Exception
    {
        File file = new File(createTempDir(), "file.txt");
        FileUtils.writeStringToFile(file, "hello");
        FilePath filePath = FilePathUtil.isFileOrNull(getFilePath("quinexistepas"), new FilePath(file));
        assertNotNull("second file exists, this should not return null", filePath);
        assertEquals(file.getAbsolutePath(), filePath.getRemote());
    }

    public void testIsDirectoryOrNullNotExists() throws Exception
    {
        assertNull("directory does not exist, this should return null",
                FilePathUtil.isDirectoryOrNull(getFilePath("quinexistepas")));
    }

    public void testIsDirectoryOrNullExistsButFile() throws Exception
    {
        File file = new File(createTempDir(), "file.txt");
        FileUtils.writeStringToFile(file, "hello");
        assertNull("file exists but is not a directory, this should return null",
                FilePathUtil.isDirectoryOrNull(new FilePath(file)));
    }

    public void testIsDirectoryOrNullExists() throws Exception
    {
        FilePath filePath = FilePathUtil.isDirectoryOrNull(new FilePath(createTempDir()));
        assertNotNull("directory exists, this should not return null", filePath);
    }

    public void testIsDirectoryOrNullSecondIsDirectory() throws Exception
    {
        File file = createTempDir();
        FilePath filePath = FilePathUtil.isDirectoryOrNull(getFilePath("quinexistepas"), new FilePath(file));
        assertNotNull("second directory exists, this should not return null", filePath);
        assertEquals(file.getAbsolutePath(), filePath.getRemote());
    }

    public void testIsFileExists() throws Exception
    {
        File file = new File(createTempDir(), "file.txt");
        FileUtils.writeStringToFile(file, "hello");
        assertTrue("file should exist", FilePathUtil.isFile(new FilePath(file)));
    }

    public void testIsFileExistsButDirectory() throws Exception
    {
        assertFalse("file should not exist, this is a directory", FilePathUtil.isFile(new FilePath(createTempDir())));
    }

    public void testIsFileNotExists() throws Exception
    {
        assertFalse("file should not exist", FilePathUtil.isFile(getFilePath("quinexistepas")));
    }

    public void testSynchronizeFileDestFileNotExists() throws Exception
    {
        File srcFile = new File(createTempDir(), "src.txt");
        FileUtils.writeStringToFile(srcFile, "hello");
        FilePath srcFilePath = new FilePath(srcFile);
        FilePath destFilePath = new FilePath(new File(getTempDir(), "dest.txt"));
        FilePathUtil.synchronize(srcFilePath, destFilePath);
        assertContentEquals(srcFilePath, destFilePath);
    }

    public void testSynchronizeFileDestFileDiffer() throws Exception
    {
        File srcFile = new File(createTempDir(), "src.txt");
        FileUtils.writeStringToFile(srcFile, "hello");
        FilePath srcFilePath = new FilePath(srcFile);
        File destFile = new File(getTempDir(), "dest.txt");
        FileUtils.writeStringToFile(srcFile, "world");
        FilePath destFilePath = new FilePath(destFile);
        FilePathUtil.synchronize(srcFilePath, destFilePath);
        assertContentEquals(srcFilePath, destFilePath);
    }

    public void testSynchronizeDirDestFileDeleted() throws Exception
    {
        File srcDir = createTempDir("src");
        FilePath srcDirFilePath = new FilePath(srcDir);
        File destDir = createTempDir("dest");
        File destFile = new File(destDir, "file.txt");
        FileUtils.writeStringToFile(destFile, "hello");
        FilePath destFilePath = new FilePath(destFile);
        FilePath destDirFilePath = new FilePath(destDir);
        FilePathUtil.synchronize(srcDirFilePath, destDirFilePath);
        assertNotExists(destFilePath);
    }

    public void testSynchronizeDirDestFileNotExists() throws Exception
    {
        File srcDir = createTempDir("src");
        FilePath srcDirFilePath = new FilePath(srcDir);
        File srcFile = new File(srcDir, "file.txt");
        FileUtils.writeStringToFile(srcFile, "hello");
        FilePath srcFilePath = new FilePath(srcFile);
        File destDir = createTempDir("dest");
        FilePath destDirFilePath = new FilePath(destDir);
        FilePath destFilePath = new FilePath(new File(destDir, "file.txt"));
        FilePathUtil.synchronize(srcDirFilePath, destDirFilePath);
        assertContentEquals(srcFilePath, destFilePath);
    }

    public void testSynchronizeDirDestFileDiffer() throws Exception
    {
        File srcDir = createTempDir("src");
        FilePath srcDirFilePath = new FilePath(srcDir);
        File srcFile = new File(srcDir, "file.txt");
        FileUtils.writeStringToFile(srcFile, "hello");
        FilePath srcFilePath = new FilePath(srcFile);
        File destDir = createTempDir("dest");
        FilePath destDirFilePath = new FilePath(destDir);
        File destFile = new File(destDir, "file.txt");
        FileUtils.writeStringToFile(srcFile, "world");
        FilePath destFilePath = new FilePath(destFile);
        FilePathUtil.synchronize(srcDirFilePath, destDirFilePath);
        assertContentEquals(srcFilePath, destFilePath);
    }
}
