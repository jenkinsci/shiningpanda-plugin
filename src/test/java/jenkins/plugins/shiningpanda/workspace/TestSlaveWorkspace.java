package jenkins.plugins.shiningpanda.workspace;

import hudson.FilePath;

import java.io.File;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;

import org.apache.commons.io.FileUtils;

public class TestSlaveWorkspace extends ShiningPandaTestCase
{

    public void testGetVirtualenvPy() throws Exception
    {
        Workspace workspace = getSlaveWorkspace();
        FilePath slavePy = workspace.getVirtualenvPy();
        FilePath masterPy = workspace.getMasterVirtualenvPy();
        assertFile(slavePy);
        assertNotSame("path of virtualenv.py on slave should differ from master one", masterPy.getRemote(), slavePy.getRemote());
        assertContentEquals(masterPy, slavePy);
    }

    public void testGetPackageDirNotExists() throws Exception
    {
        assertNull("slave workspace should not have a package directory", getMasterWorkspace().getPackagesDir());
    }

    public void testGetPackageDirExists() throws Exception
    {
        File masterPackagesDir = createPackagesDir();
        File masterPackagesFile = new File(masterPackagesDir, "toto.txt");
        FileUtils.writeStringToFile(masterPackagesFile, "hello");
        FilePath slavePackagesDir = getSlaveWorkspace().getPackagesDir();
        File slavePackagesFile = new File(toFile(slavePackagesDir), "toto.txt");
        assertContentEquals(masterPackagesFile, slavePackagesFile);
        assertNotSame("path of package on slave should differ from master one", masterPackagesFile.getAbsolutePath(),
                slavePackagesFile.getAbsoluteFile());
    }

}
