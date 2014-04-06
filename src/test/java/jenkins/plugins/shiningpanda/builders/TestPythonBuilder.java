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
package jenkins.plugins.shiningpanda.builders;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import java.util.List;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.command.CommandNature;
import jenkins.plugins.shiningpanda.matrix.PythonAxis;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

import org.apache.commons.io.FileUtils;

public class TestPythonBuilder extends ShiningPandaTestCase
{

    public void testRoundTripFreeStyle() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        PythonBuilder before = new PythonBuilder(installation.getName(), CommandNature.SHELL.getKey(), "echo hello", true);
        PythonBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "pythonName,nature,command,ignoreExitCode");
    }

    public void testRoundTripMatrix() throws Exception
    {
        PythonBuilder before = new PythonBuilder("foobar", CommandNature.XSHELL.getKey(), "echo hello", false);
        PythonBuilder after = configPythonMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "command,nature,ignoreExitCode");
    }

    public void testHomeWithSpace() throws Exception
    {
        PythonInstallation installation = configurePython("Python", createFakePythonInstallationWithWhitespaces()
                .getAbsolutePath());
        PythonBuilder builder = new PythonBuilder(installation.getName(), CommandNature.SHELL.getKey(), "echo hello", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("whitespace should not have been allowed:\n" + log,
                log.contains(Messages.BuilderUtil_Interpreter_WhitespaceNotAllowed("")));
    }

    public void testTextAxisValueAvailable() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        PythonBuilder builder = new PythonBuilder(null, CommandNature.SHELL.getKey(), "echo \"Welcome $TOTO\"", false);
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
        PythonBuilder builder = new PythonBuilder(name, CommandNature.SHELL.getKey(), "echo \"Welcome $TOTO\"", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("an invalid installation name was expected:\n" + log,
                log.contains(Messages.BuilderUtil_Installation_NotFound(name)));
    }

    public void testNoInstallation() throws Exception
    {
        String name = "Toto";
        PythonBuilder builder = new PythonBuilder(name, CommandNature.SHELL.getKey(), "echo \"Welcome $TOTO\"", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("a missing installation was expected:\n" + log,
                log.contains(Messages.BuilderUtil_Installation_NotFound(name)));
    }

    public void testNoPythonAxis() throws Exception
    {
        configureCPython2();
        PythonBuilder builder = new PythonBuilder(null, CommandNature.SHELL.getKey(), "echo \"Welcome $TOTO\"", false);
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
        PythonBuilder builder = new PythonBuilder(installation.getName(), CommandNature.SHELL.getKey(), "ls foobartrucmuch",
                true);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
    }

    public void testConsiderExitCode() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        PythonBuilder builder = new PythonBuilder(installation.getName(), CommandNature.SHELL.getKey(), "ls foobartrucmuch",
                false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have failed:\n" + log, log.contains("FAILURE"));
    }

    public void testPythonNature() throws Exception
    {

        PythonInstallation installation = configureCPython2();
        PythonBuilder builder = new PythonBuilder(installation.getName(), CommandNature.PYTHON.getKey(),
                "import sys\nsys.stdout.write('hello world!\\n')", false);
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
        PythonBuilder builder = new PythonBuilder(installation.getName(), CommandNature.XSHELL.getKey(), "echo %HOME%", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
        String home = System.getProperty("user.home");
        assertTrue("this build should have say " + home + ":\n" + log, log.contains(home));
    }

}
