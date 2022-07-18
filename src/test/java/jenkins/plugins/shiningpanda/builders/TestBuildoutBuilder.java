/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2015 ShiningPanda S.A.S.
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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.command.CommandNature;
import jenkins.plugins.shiningpanda.scm.BuildoutSCM;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import org.apache.commons.io.FileUtils;

import static org.junit.Assert.assertTrue;

public class TestBuildoutBuilder extends ShiningPandaTestCase {

    public void testRoundTripFreeStyle() throws Exception {
        PythonInstallation installation = configureCPython2();
        BuildoutBuilder before = new BuildoutBuilder(installation.getName(), "foo/buildout.cfg",
                CommandNature.SHELL.getKey(), "echo hello", true);
        BuildoutBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "pythonName,buildoutCfg,nature,command,ignoreExitCode");
    }

    public void testRoundTripMatrix() throws Exception {
        BuildoutBuilder before = new BuildoutBuilder("foobar", "bar/dev.cfg", CommandNature.PYTHON.getKey(),
                "echo hello", false);
        BuildoutBuilder after = configPythonMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "buildoutCfg,nature,command,ignoreExitCode");
    }

    public void test() throws Exception {
        PythonInstallation installation = configureCPython3();
        String buildoutCfg = "buildout.cfg";
        String djangoProject = "HelloWorld";
        BuildoutBuilder builder = new BuildoutBuilder(installation.getName(), buildoutCfg, CommandNature.SHELL.getKey(),
                "django --help", true);
        FreeStyleProject project = j.createFreeStyleProject();
        StringBuffer sb = new StringBuffer();
        sb.append("[buildout]").append("\n");
        sb.append("parts = django").append("\n");
        sb.append("[django]").append("\n");
        sb.append("recipe = djangorecipe").append("\n");
        sb.append("project = " + djangoProject).append("\n");
        sb.append("settings = settings").append("\n");
        project.setScm(new BuildoutSCM(buildoutCfg, sb.toString(), djangoProject));
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have displayed django help:\n" + log,
                log.contains("Type 'django help <subcommand>' for help on a specific subcommand"));
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
    }
}
