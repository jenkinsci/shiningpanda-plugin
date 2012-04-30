/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2012 ShiningPanda S.A.S.
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

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.actions.coverage.CoverageBuildAction;
import jenkins.plugins.shiningpanda.actions.coverage.CoverageProjectAction;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class CoveragePublisher extends Recorder
{

    /**
     * Base name for the HTML report folder on master.
     */
    public final static String BASENAME = "coveragepy";

    /**
     * Script file contained in report folders.
     */
    public final static String JS = "coverage_html.js";

    /**
     * Index file in report folders.
     */
    public final static String INDEX = "index.html";

    /**
     * Status file in report folders.
     */
    public final static String STATUS = "status.dat";

    /**
     * Path to the HTML folder in the workspace.
     */
    public final String htmlDir;

    /**
     * Constructor using fields.
     * 
     * @param htmlDir
     *            The HTML directory
     */
    @DataBoundConstructor
    public CoveragePublisher(String htmlDir)
    {
        // Call super
        super();
        // Store the HTML directory
        this.htmlDir = Util.fixEmptyAndTrim(htmlDir);
    }

    /**
     * Look for HTML reports.
     * 
     * @param workspace
     *            The workspace
     * @param environment
     *            If environment
     */
    private List<FilePath> getHtmlDirs(FilePath workspace, EnvVars environment) throws IOException, InterruptedException
    {
        // Get the candidates
        FilePath[] candidates;
        // If an HTML folder is not specified look for it in all workspace
        if (htmlDir == null)
            // List all folders containing a coverage_html.js file
            candidates = workspace.list("**/" + JS);
        // Else use the provided value
        else
            // List all folders containing a coverage_html.js file
            candidates = workspace.list(environment.expand(htmlDir) + "/" + JS);
        // Store the list of matching folders
        List<FilePath> dirs = new ArrayList<FilePath>();
        // Go threw the candidates
        for (FilePath js : candidates)
            // Check that following files also exists
            if (js.sibling(INDEX).exists() && js.sibling(STATUS).exists())
                // Add the parent folder
                dirs.add(js.getParent());
        // Return the found directories
        return dirs;
    }

    /**
     * Get the HTML target folder.
     * 
     * @param base
     *            The base target directory
     * @param workspace
     *            The workspace
     * @param dir
     *            The directory in workspace containing reports
     * @param single
     *            Is there more than one report
     */
    private FilePath getHtmlTargetDir(FilePath base, FilePath workspace, FilePath dir, boolean single)
    {
        // If only one report, copy it in the base folder
        if (single)
            // Return the base folder
            return base;
        // Else replicate the workspace tree. Get the workspace path.
        String ws = workspace.getRemote();
        // Get the index to delete the workspace path
        int beginIndex = ws.endsWith("/") || ws.endsWith("\\") ? ws.length() : ws.length() + 1;
        // Delete workspace part
        return base.child(dir.getRemote().substring(beginIndex));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild
     * , hudson.Launcher, hudson.model.BuildListener)
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException,
            IOException
    {
        // Get the workspace
        FilePath workspace = build.getWorkspace();
        // Look for HTML reports
        List<FilePath> dirs = getHtmlDirs(build.getWorkspace(), build.getEnvironment(listener));
        // If no reports, log and modify build result
        if (dirs.size() == 0)
        {
            // If build is already unstable log nothing
            if (build.getResult().isBetterOrEqualTo(Result.UNSTABLE))
                // Else log that nothing found
                listener.error(Messages.CoverageArchiver_HtmlDir_NotFound());
            // Set build as failure
            build.setResult(Result.FAILURE);
            // Go on
            return true;
        }
        // Get the base target folder
        FilePath base = new FilePath(getHtmlDir(build));
        // Cleanup
        base.deleteRecursive();
        // Go threw the report folders
        for (FilePath dir : dirs)
        {
            // Get the target folder
            FilePath targetDir = getHtmlTargetDir(base, workspace, dir, dirs.size() == 1);
            // Only copy files if not already exists, we do not handle included
            // reports
            if (!targetDir.exists())
                // Copy their contents
                dir.copyRecursiveTo("**/*", targetDir);
        }
        // Add the build action
        build.addAction(new CoverageBuildAction(build));
        // Go on
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * hudson.tasks.BuildStepCompatibilityLayer#getProjectActions(hudson.model
     * .AbstractProject)
     */
    @Override
    public Collection<Action> getProjectActions(AbstractProject<?, ?> project)
    {
        // Return the project action
        return Collections.<Action> singleton(new CoverageProjectAction(project));
    }

    /*
     * (non-Javadoc)
     * 
     * @see hudson.tasks.BuildStep#getRequiredMonitorService()
     */
    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.NONE;
    }

    /**
     * Get the folder containing the HTML on the master for a build.
     * 
     * @param run
     *            The build
     * @return The path to the HTML folder
     */
    public static File getHtmlDir(Run<?, ?> run)
    {
        // Check if the provided run exists
        return run == null ? null : new File(run.getRootDir(), BASENAME);
    }

    /**
     * Recorder descriptor.
     */
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher>
    {
        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getDisplayName()
         */
        public String getDisplayName()
        {
            return Messages.CoverageArchiver_DisplayName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile()
        {
            return Functions.getResourcePath() + "/plugin/shiningpanda/help/publishers/CoveragePublisher/help.html";
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
         */
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType)
        {
            // Always applicable
            return true;
        }

        /**
         * 
         * Check if the HTML folder is relative to the workspace.
         * 
         * @param project
         *            The project
         * @param value
         *            The value to check
         * @return Success if the value is relative
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckHtmlDir(@AncestorInPath AbstractProject<?, ?> project, @QueryParameter String value)
                throws IOException, ServletException
        {
            // Get a workspace
            FilePath workspace = project.getSomeWorkspace();
            // If a workspace is available, check that the value is relative
            return workspace != null ? workspace.validateFileMask(value) : FormValidation.ok();
        }
    }
}
