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
        File htmlcov = new File(project.getLastSuccessfulBuild().getRootDir(), "htmlcov");
        assertTrue("htmlcov folder should have been created: " + htmlcov.getAbsolutePath(), htmlcov.exists());
    }

    public void testHtmlDir() throws Exception
    {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new CoverageSCM("htmlcov"));
        project.getPublishersList().add(new CoveragePublisher("htmlcov"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
        File htmlcov = new File(project.getRootDir(), "htmlcov");
        assertTrue("htmlcov folder should have been created: " + htmlcov.getAbsolutePath(), htmlcov.exists());
    }

    public void testMultipleHtmlDir() throws Exception
    {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new CoverageSCM("htmlcov", "toto/htmlcov"));
        project.getPublishersList().add(new CoveragePublisher(null));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
        File htmlcov = new File(project.getRootDir(), "htmlcov");
        assertTrue("htmlcov folder should have been created: " + htmlcov.getAbsolutePath(), htmlcov.exists());
        assertTrue("missing report under htmlcov folder", new File(htmlcov, "htmlcov").exists());
        assertTrue("missing report under htmlcov folder", new File(htmlcov, "toto/htmlcov").exists());
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
