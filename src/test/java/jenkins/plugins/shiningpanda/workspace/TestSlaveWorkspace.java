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

    public void testGetBootstrapPy() throws Exception
    {
        Workspace workspace = getSlaveWorkspace();
        FilePath slavePy = workspace.getBootstrapPy();
        FilePath masterPy = workspace.getMasterBootstrapPy();
        assertFile(slavePy);
        assertNotSame("path of virtualenv.py on slave should differ from master one", masterPy.getRemote(), slavePy.getRemote());
        assertContentEquals(masterPy, slavePy);
    }

    public void testGetPackageDirNotExists() throws Exception
    {
        assertNull("slave workspace should not have a package directory", getSlaveWorkspace().getPackagesDir());
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
