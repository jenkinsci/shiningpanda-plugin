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

import hudson.FilePath;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import java.io.File;
import java.util.List;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.builders.VirtualenvBuilder;
import jenkins.plugins.shiningpanda.command.CommandNature;
import jenkins.plugins.shiningpanda.matrix.PythonAxis;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

import org.apache.commons.io.FileUtils;

public class TestWorkspace extends ShiningPandaTestCase
{

    public void testGetVirtualenvPy() throws Exception
    {
        Workspace workspace = getWorkspace();
        FilePath slavePy = workspace.getVirtualenvPy();
        assertFile(slavePy);
    }

    public void testGetBootstrapPy() throws Exception
    {
        Workspace workspace = getWorkspace();
        FilePath slavePy = workspace.getBootstrapPy();
        assertFile(slavePy);
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

    public void testDeleteFreeStyle() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", true, true, false,
                CommandNature.SHELL.getKey(), "echo", true);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
        Workspace workspace = Workspace.fromBuild(build);
        assertTrue("worspace should exist: " + workspace.getHome().getRemote(), workspace.getHome().exists());
        Workspace.delete(project);
        assertFalse("worspace should not exist: " + workspace.getHome().getRemote(), workspace.getHome().exists());
    }

    public void testDeleteMatrix() throws Exception
    {
        PythonInstallation installation2 = configureCPython2();
        PythonInstallation installation3 = configureCPython3();
        VirtualenvBuilder builder = new VirtualenvBuilder(null, "env", true, true, false, CommandNature.SHELL.getKey(), "echo",
                false);
        MatrixProject project = createMatrixProject();
        AxisList axes = new AxisList(new PythonAxis(new String[] { installation2.getName(), installation3.getName() }));
        project.setAxes(axes);
        project.getBuildersList().add(builder);
        MatrixBuild build = project.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(2, runs.size());

        for (MatrixRun run : runs)
        {
            String log = FileUtils.readFileToString(run.getLogFile());
            assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
            Workspace workspace = Workspace.fromBuild(run);
            assertTrue("worspace should exist: " + workspace.getHome().getRemote(), workspace.getHome().exists());
        }
        Workspace.delete(project);
        for (MatrixRun run : runs)
        {
            Workspace workspace = Workspace.fromBuild(run);
            assertFalse("worspace should not exist: " + workspace.getHome().getRemote(), workspace.getHome().exists());
        }
    }

}
