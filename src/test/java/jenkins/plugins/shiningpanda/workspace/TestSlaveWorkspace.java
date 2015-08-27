/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2014 ShiningPanda S.A.S.
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
package jenkins.plugins.shiningpanda.workspace;

import java.io.File;

import org.apache.commons.io.FileUtils;

import hudson.FilePath;
import jenkins.plugins.shiningpanda.ShiningPandaTestCase;

public class TestSlaveWorkspace extends ShiningPandaTestCase {

    public void testGetPackageDirNotExists() throws Exception {
	assertNull("slave workspace should not have a package directory", getSlaveWorkspace().getPackagesDir());
    }

    public void testGetPackageDirExists() throws Exception {
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
