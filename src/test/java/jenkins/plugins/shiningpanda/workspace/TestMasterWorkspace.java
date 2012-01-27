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

import java.io.File;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;

public class TestMasterWorkspace extends ShiningPandaTestCase
{

    public void testGetVirtualenvPy() throws Exception
    {
        assertEquals(getMasterWorkspace().getMasterVirtualenvPy(), getMasterWorkspace().getVirtualenvPy());
    }

    public void testGetBootstrapPy() throws Exception
    {
        assertEquals(getMasterWorkspace().getMasterBootstrapPy(), getMasterWorkspace().getBootstrapPy());
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
