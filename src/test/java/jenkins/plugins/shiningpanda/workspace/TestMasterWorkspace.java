package jenkins.plugins.shiningpanda.workspace;

import java.io.File;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;

public class TestMasterWorkspace extends ShiningPandaTestCase
{

    public void testGetVirtualenvPy() throws Exception
    {
        assertEquals(getMasterWorkspace().getMasterVirtualenvPy(), getMasterWorkspace().getVirtualenvPy());
    }

    public void testGetPackageDirNotExists() throws Exception
    {
        assertNull("master workspace should not have a package directory", getMasterWorkspace().getPackagesDir());
    }

    public void testGetPackageDirExists() throws Exception
    {
        File packagesDir = createPackagesDir();
        assertEquals("invalid package directory", packagesDir.getPath(), getWorkspace().getPackagesDir().getRemote());
    }

}
