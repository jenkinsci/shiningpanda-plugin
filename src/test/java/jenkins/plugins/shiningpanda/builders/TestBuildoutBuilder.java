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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.command.CommandNature;
import jenkins.plugins.shiningpanda.scm.BuildoutSCM;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

import org.apache.commons.io.FileUtils;

public class TestBuildoutBuilder extends ShiningPandaTestCase
{

    public void testRoundTripFreeStyle() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        BuildoutBuilder before = new BuildoutBuilder(installation.getName(), "foo/buildout.cfg", true,
                CommandNature.SHELL.getKey(), "echo hello", true);
        BuildoutBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "pythonName,buildoutCfg,useDistribute,nature,command,ignoreExitCode");
    }

    public void testRoundTripMatrix() throws Exception
    {
        BuildoutBuilder before = new BuildoutBuilder("foobar", "bar/dev.cfg", false, CommandNature.PYTHON.getKey(),
                "echo hello", false);
        BuildoutBuilder after = configPythonMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "buildoutCfg,useDistribute,nature,command,ignoreExitCode");
    }

    public void test() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        String buildoutCfg = "buildout.cfg";
        BuildoutBuilder builder = new BuildoutBuilder(installation.getName(), buildoutCfg, true, CommandNature.SHELL.getKey(),
                "django --help", true);
        FreeStyleProject project = createFreeStyleProject();
        StringBuffer sb = new StringBuffer();
        sb.append("[buildout]").append("\n");
        sb.append("parts = django").append("\n");
        sb.append("[django]").append("\n");
        sb.append("recipe = djangorecipe").append("\n");
        sb.append("project = HelloWorld").append("\n");
        project.setScm(new BuildoutSCM(buildoutCfg, sb.toString()));
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have displayed django help:\n" + log,
                log.contains("Usage: django subcommand [options] [args]"));
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
    }
}
