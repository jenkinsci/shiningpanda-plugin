/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2013 ShiningPanda S.A.S.
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
package jenkins.plugins.shiningpanda.builders;

import hudson.FilePath;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import java.io.File;
import java.util.List;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.command.CommandNature;
import jenkins.plugins.shiningpanda.matrix.PythonAxis;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

import jenkins.plugins.shiningpanda.workspace.Workspace;
import org.apache.commons.io.FileUtils;

public class TestVirtualenvBuilder extends ShiningPandaTestCase
{

    public static final String PACKAGE_NAME = "tempdir";

    public void testRoundTripFreeStyle() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        VirtualenvBuilder before = new VirtualenvBuilder(installation.getName(), "env2", true, false, false,
                CommandNature.SHELL.getKey(), "echo hello", true, "");
        VirtualenvBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "pythonName,home,clear,useDistribute,systemSitePackages,nature,command,ignoreExitCode");
    }

    public void testRoundTripMatrix() throws Exception
    {
        VirtualenvBuilder before = new VirtualenvBuilder("foobar", "env2", true, false, false, CommandNature.PYTHON.getKey(),
                "echo hello", false, "");
        VirtualenvBuilder after = configPythonMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "home,clear,useDistribute,systemSitePackages,nature,command,ignoreExitCode");
    }

    public void testStandardPythonHomeWithSpace() throws Exception
    {
        PythonInstallation installation = configurePython("Python", createFakePythonInstallationWithWhitespaces()
                .getAbsolutePath());
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", false, true, false,
                CommandNature.SHELL.getKey(), "echo hello", false, "");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("whitespace should not have been allowed:\n" + log,
                log.contains(Messages.BuilderUtil_Interpreter_WhitespaceNotAllowed("")));
    }

    public void testTextAxisAvailable() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(null, "env", true, true, false, CommandNature.SHELL.getKey(),
                "echo \"Welcome $TOTO\"", false, "");
        MatrixProject project = createMatrixProject();
        AxisList axes = new AxisList(new PythonAxis(new String[] { installation.getName(), }), new TextAxis("TOTO", "TUTU"));
        project.setAxes(axes);
        project.getBuildersList().add(builder);
        MatrixBuild build = project.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(1, runs.size());
        MatrixRun run = runs.get(0);
        String log = FileUtils.readFileToString(run.getLogFile());
        assertTrue("value of text axis not available in builder:\n" + log, log.contains("Welcome TUTU"));
    }

    public void testInvalidPythonName() throws Exception
    {
        configureCPython2();
        String name = "Toto";
        VirtualenvBuilder builder = new VirtualenvBuilder(name, "env", true, true, false, CommandNature.SHELL.getKey(),
                "echo \"Welcome $TOTO\"", false, "");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("an invalid name was expected:\n" + log, log.contains(Messages.BuilderUtil_Installation_NotFound(name)));
    }

    public void testNoPython() throws Exception
    {
        String name = "Toto";
        VirtualenvBuilder builder = new VirtualenvBuilder(name, "env", true, true, false, CommandNature.SHELL.getKey(),
                "echo \"Welcome $TOTO\"", false, "");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("an missing installation was expetced:\n" + log,
                log.contains(Messages.BuilderUtil_Installation_NotFound(name)));
    }

    public void testNoPythonAxis() throws Exception
    {
        configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(null, "env", true, true, false, CommandNature.SHELL.getKey(),
                "echo \"Welcome $TOTO\"", false, "");
        MatrixProject project = createMatrixProject();
        AxisList axes = new AxisList(new TextAxis("TOTO", "TUTU"));
        project.setAxes(axes);
        project.getBuildersList().add(builder);
        MatrixBuild build = project.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(1, runs.size());
        MatrixRun run = runs.get(0);
        String log = FileUtils.readFileToString(run.getLogFile());
        assertTrue("a missing python axis was expected:\n" + log, log.contains(Messages.BuilderUtil_PythonAxis_Required()));
    }

    public void testIgnoreExitCode() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", true, true, false,
                CommandNature.SHELL.getKey(), "ls foobartrucmuch", true, "");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
    }

    public void testConsiderExitCode() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", true, true, false,
                CommandNature.SHELL.getKey(), "ls foobartrucmuch", false, "");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have failed:\n" + log, log.contains("FAILURE"));
    }

    public void testNoVirtualenvClear() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", false, true, false,
                CommandNature.SHELL.getKey(), "echo", false, "");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("a new virtualenv was expected to be created:\n" + log, log.contains("New python executable in"));
        build = project.scheduleBuild2(0).get();
        log = FileUtils.readFileToString(build.getLogFile());
        assertFalse("a new virtualenv should not have been created:\n" + log, log.contains("New python executable in"));
    }

    public void testVirtualenvClear() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", true, true, false,
                CommandNature.SHELL.getKey(), "echo", false, "");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("a new virtualenv was expected to be created:\n" + log, log.contains("New python executable in"));
        build = project.scheduleBuild2(0).get();
        log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("a new virtualenv was expected to be created:\n" + log, log.contains("New python executable in"));
    }

    public void testVirtualenvClearOnChange() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", false, true, false,
                CommandNature.SHELL.getKey(), "echo", false, "");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("a new virtualenv was expected to be created:\n" + log, log.contains("New python executable in"));
        builder.systemSitePackages = true;
        build = project.scheduleBuild2(0).get();
        log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("a new virtualenv was expected to be created:\n" + log, log.contains("New python executable in"));
    }

    public void testPythonNature() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", true, true, false,
                CommandNature.PYTHON.getKey(), "import sys\nsys.stdout.write('hello world!\\n')", true, "");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
        assertTrue("this build should have say hello world:\n" + log, log.contains("hello world!"));
    }

    public void testXShellNature() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", true, true, false,
                CommandNature.XSHELL.getKey(), "echo %HOME%", true, "");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
        String home = System.getProperty("user.home");
        assertTrue("this build should have say " + home + ":\n" + log, log.contains(home));
    }

    public void testInstallsRequirementsFromFile() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        Workspace workspace = getWorkspace();
        FilePath workspaceHome = workspace.getHome();
        File requirementsFile = toFile(workspaceHome.child("requirements.txt"));
        FileUtils.writeStringToFile(requirementsFile, PACKAGE_NAME);
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", true, true, false,
                CommandNature.XSHELL.getKey(), "echo %HOME%", true, requirementsFile.getAbsolutePath());
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have installed " + PACKAGE_NAME,
                log.contains("collected packages: "+PACKAGE_NAME));
    }
}
