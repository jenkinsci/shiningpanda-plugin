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
package jenkins.plugins.shiningpanda.publishers;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import java.io.File;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.scm.CoverageSCM;

import org.apache.commons.io.FileUtils;

public class TestCoveragePublisher extends ShiningPandaTestCase
{

    public void testRoundTrip() throws Exception
    {
        CoveragePublisher before = new CoveragePublisher("**/htmlcov");
        CoveragePublisher after = configRoundtrip(before);
        assertEqualBeans2(before, after, "htmlDir");
    }

    public void testNoHtmlDir() throws Exception
    {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new CoverageSCM("htmlcov"));
        project.getPublishersList().add(new CoveragePublisher(null));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
        File coveragepy = new File(project.getLastSuccessfulBuild().getRootDir(), CoveragePublisher.BASENAME);
        assertTrue("htmlcov folder should have been created: " + coveragepy.getAbsolutePath(), coveragepy.exists());
    }

    public void testHtmlDir() throws Exception
    {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new CoverageSCM("htmlcov"));
        project.getPublishersList().add(new CoveragePublisher("htmlcov"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
        File coveragepy = new File(project.getLastSuccessfulBuild().getRootDir(), CoveragePublisher.BASENAME);
        assertTrue("htmlcov folder should have been created: " + coveragepy.getAbsolutePath(), coveragepy.exists());
    }

    public void testMultipleHtmlDir() throws Exception
    {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new CoverageSCM("htmlcov", "toto/htmlcov"));
        project.getPublishersList().add(new CoveragePublisher(null));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
        File coveragepy = new File(project.getLastSuccessfulBuild().getRootDir(), CoveragePublisher.BASENAME);
        assertTrue("htmlcov folder should have been created: " + coveragepy.getAbsolutePath(), coveragepy.exists());
        assertTrue("missing report under htmlcov folder", new File(coveragepy, "htmlcov").exists());
        assertTrue("missing report under htmlcov folder", new File(coveragepy, "toto/htmlcov").exists());
    }

    public void testHtmlDirNotExists() throws Exception
    {
        FreeStyleProject project = createFreeStyleProject();
        project.getPublishersList().add(new CoveragePublisher(null));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have failed:\n" + log, log.contains("FAILURE"));
    }

}
