package jenkins.plugins.shiningpanda.workspace;

import java.io.File;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;

public class TestWorkspace extends ShiningPandaTestCase
{

    public void testIsUnix() throws Exception
    {
        assertTrue("this should be an UNIX workspace", getWorkspace().isUnix());
    }

    public void testIsWindows() throws Exception
    {
        assertFalse("this should not be a Windows workspace", getWorkspace().isWindows());
    }

    public void testGetMasterVirtualenvPy() throws Exception
    {
        assertFile(getWorkspace().getMasterVirtualenvPy());
    }

    public void testGetMasterPackageDirNotExists() throws Exception
    {
        assertNull("workspace should not have a package directory", getWorkspace().getMasterPackagesDir());
    }

    public void testGetMasterPackageDirExists() throws Exception
    {
        File packagesDir = createPackagesDir();
        assertEquals("invalid package directory", packagesDir.getPath(), getWorkspace().getMasterPackagesDir().getRemote());
    }

}
