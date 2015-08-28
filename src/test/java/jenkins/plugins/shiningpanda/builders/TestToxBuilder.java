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

import java.util.List;

import org.apache.commons.io.FileUtils;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.matrix.TextAxis;
import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.matrix.ToxAxis;
import jenkins.plugins.shiningpanda.scm.ToxSCM;

public class TestToxBuilder extends ShiningPandaTestCase {

    public void testRoundTrip() throws Exception {
	ToxBuilder before = new ToxBuilder("toto/tox.ini", true, "$TOTO");
	ToxBuilder after = configToxMatrixRoundtrip(before);
	assertEqualBeans2(before, after, "toxIni,recreate,toxenvPattern");
    }

    public void testNoAxis() throws Exception {
	ToxBuilder builder = new ToxBuilder("tox.ini", false, null);
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

    public void testNoInterpreter() throws Exception {
	ToxBuilder builder = new ToxBuilder("tox.ini", false, null);
	MatrixProject project = createMatrixProject();
	AxisList axes = new AxisList(new ToxAxis(new String[] { "py27" }));
	project.setAxes(axes);
	project.getBuildersList().add(builder);
	MatrixBuild build = project.scheduleBuild2(0).get();
	List<MatrixRun> runs = build.getRuns();
	assertEquals(1, runs.size());
	MatrixRun run = runs.get(0);
	String log = FileUtils.readFileToString(run.getLogFile());
	assertTrue("should not have found an interpreter:\n" + log,
		log.contains(Messages.BuilderUtil_NoInterpreterFound()));
    }

    public void testToxAxisSuccessful() throws Exception {
	configureCPython2();
	ToxBuilder builder = new ToxBuilder("tox.ini", false, null);
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

    public void testToxenvPatternSuccessful() throws Exception {
	configureCPython2();
	ToxBuilder builder = new ToxBuilder("tox.ini", false, "$INTERPRETER$VERSION");
	MatrixProject project = createMatrixProject();
	AxisList axes = new AxisList(new TextAxis("INTERPRETER", "py"), new TextAxis("VERSION", "27"));
	project.setScm(new ToxSCM("tox.ini", "[testenv]\ncommand = echo"));
	project.setAxes(axes);
	project.getBuildersList().add(builder);
	MatrixBuild build = project.scheduleBuild2(0).get();
	List<MatrixRun> runs = build.getRuns();
	assertEquals(1, runs.size());
	MatrixRun run = runs.get(0);
	String log = FileUtils.readFileToString(run.getLogFile());
	System.out.println(log);
	assertTrue("tox should have been successful:\n" + log, log.contains("congratulations :)"));
	assertTrue("build should have been successful:\n" + log, log.contains("SUCCESS"));
    }

    public void testToxAxisAndToxenvPattern() throws Exception {
	configureCPython2();
	ToxBuilder builder = new ToxBuilder("tox.ini", false, "$INTERPRETER$VERSION");
	MatrixProject project = createMatrixProject();
	AxisList axes = new AxisList(new ToxAxis(new String[] { "py27" }), new TextAxis("INTERPRETER", "py"),
		new TextAxis("VERSION", "27"));
	project.setScm(new ToxSCM("tox.ini", "[testenv]\ncommand = echo"));
	project.setAxes(axes);
	project.getBuildersList().add(builder);
	MatrixBuild build = project.scheduleBuild2(0).get();
	List<MatrixRun> runs = build.getRuns();
	assertEquals(1, runs.size());
	MatrixRun run = runs.get(0);
	String log = FileUtils.readFileToString(run.getLogFile());
	System.out.println(log);
	assertTrue("should not be able to run with both Tox axis and TOXENV pattern:\n" + log,
		log.contains(Messages.ToxBuilder_ToxAxis_And_ToxenvPattern()));
    }

    public void testToxenvPatternBlank() throws Exception {
	configureCPython2();
	ToxBuilder builder = new ToxBuilder("tox.ini", false, "$FOOBAR");
	MatrixProject project = createMatrixProject();
	AxisList axes = new AxisList(new TextAxis("FOOBAR2", "badluck"));
	project.setScm(new ToxSCM("tox.ini", "[testenv]\ncommand = echo"));
	project.setAxes(axes);
	project.getBuildersList().add(builder);
	MatrixBuild build = project.scheduleBuild2(0).get();
	List<MatrixRun> runs = build.getRuns();
	assertEquals(1, runs.size());
	MatrixRun run = runs.get(0);
	String log = FileUtils.readFileToString(run.getLogFile());
	System.out.println(log);
	assertTrue("should not be able to run with a blank TOXENV pattern:\n" + log,
		log.contains(Messages.ToxBuilder_ToxenvPattern_Invalid("$FOOBAR")));
    }

}
