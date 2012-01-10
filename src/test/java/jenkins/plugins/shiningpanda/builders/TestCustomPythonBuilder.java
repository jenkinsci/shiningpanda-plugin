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

import org.apache.commons.io.FileUtils;

public class TestCustomPythonBuilder extends ShiningPandaTestCase
{

    public void testRoundTripFreeStyle() throws Exception
    {
        CustomPythonBuilder before = new CustomPythonBuilder("/tmp/custom", CommandNature.SHELL.getKey(), "echo hello", true);
        CustomPythonBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "home,nature,command,ignoreExitCode");
    }

    public void testRoundTripMatrix() throws Exception
    {
        CustomPythonBuilder before = new CustomPythonBuilder("/tmp/custom", CommandNature.PYTHON.getKey(), "echo hello", false);
        CustomPythonBuilder after = configPythonMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "home,nature,command,ignoreExitCode");
    }

    public void testHomeWithSpace() throws Exception
    {
        CustomPythonBuilder builder = new CustomPythonBuilder(createFakePythonInstallationWithWhitespaces().getAbsolutePath(),
                CommandNature.SHELL.getKey(), "echo hello", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("whitespace should not have been allowed:\n" + log,
                log.contains(Messages.BuilderUtil_Interpreter_WhitespaceNotAllowed("")));
    }

    public void testTextAxisAvailable() throws Exception
    {
        CustomPythonBuilder builder = new CustomPythonBuilder(getCPython2Home(), CommandNature.SHELL.getKey(),
                "echo \"Welcome $TOTO\"", false);
        MatrixProject project = createMatrixProject();
        AxisList axes = new AxisList(new TextAxis("TOTO", "TUTU"));
        project.setAxes(axes);
        project.getBuildersList().add(builder);
        MatrixBuild build = project.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(1, runs.size());
        MatrixRun run = runs.get(0);
        String log = FileUtils.readFileToString(run.getLogFile());
        assertTrue("value of text axis not available in builder:\n" + log, log.contains("Welcome TUTU"));
    }

    public void testIgnoreExitCode() throws Exception
    {
        CustomPythonBuilder builder = new CustomPythonBuilder(getCPython3Home(), CommandNature.SHELL.getKey(),
                "ls foobartrucmuch", true);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
    }

    public void testConsiderExitCode() throws Exception
    {
        CustomPythonBuilder builder = new CustomPythonBuilder(getJythonHome(), CommandNature.SHELL.getKey(),
                "ls foobartrucmuch", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have failed:\n" + log, log.contains("FAILURE"));
    }

}
