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

import java.util.List;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.matrix.ToxAxis;
import jenkins.plugins.shiningpanda.scm.ToxSCM;

import org.apache.commons.io.FileUtils;

public class TestToxBuilder extends ShiningPandaTestCase
{

    public void testRoundTrip() throws Exception
    {
        ToxBuilder before = new ToxBuilder("toto/tox.ini", true);
        ToxBuilder after = configToxMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "toxIni,recreate");
    }

    public void testNoAxis() throws Exception
    {
        ToxBuilder builder = new ToxBuilder("tox.ini", false);
        MatrixProject project = createMatrixProject();
        AxisList axes = new AxisList(new TextAxis("TOTO", "TUTU"));
        project.setAxes(axes);
        project.getBuildersList().add(builder);
        MatrixBuild build = project.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(1, runs.size());
        MatrixRun run = runs.get(0);
        String log = FileUtils.readFileToString(run.getLogFile());
        assertTrue("should not have found a tox axis:\n" + log, log.contains(Messages.ToxBuilder_ToxAxis_Required()));
    }

    public void testNoInterpreter() throws Exception
    {
        ToxBuilder builder = new ToxBuilder("tox.ini", false);
        MatrixProject project = createMatrixProject();
        AxisList axes = new AxisList(new ToxAxis(new String[] { "py27" }));
        project.setAxes(axes);
        project.getBuildersList().add(builder);
        MatrixBuild build = project.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(1, runs.size());
        MatrixRun run = runs.get(0);
        String log = FileUtils.readFileToString(run.getLogFile());
        assertTrue("should not have found an interpreter:\n" + log, log.contains(Messages.BuilderUtil_NoInterpreterFound()));
    }

    public void testToxSuccessful() throws Exception
    {
        configureCPython2();
        ToxBuilder builder = new ToxBuilder("tox.ini", false);
        MatrixProject project = createMatrixProject();
        AxisList axes = new AxisList(new ToxAxis(new String[] { "py27" }));
        project.setScm(new ToxSCM("tox.ini", "[testenv]\ncommand = echo"));
        project.setAxes(axes);
        project.getBuildersList().add(builder);
        MatrixBuild build = project.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(1, runs.size());
        MatrixRun run = runs.get(0);
        String log = FileUtils.readFileToString(run.getLogFile());
        assertTrue("tox should have been successful:\n" + log, log.contains("congratulations :)"));
        assertTrue("build should have been successful:\n" + log, log.contains("SUCCESS"));
    }

}
