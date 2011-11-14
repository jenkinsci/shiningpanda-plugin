/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011 ShiningPanda S.A.S.
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

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.ShiningPanda;
import jenkins.plugins.shiningpanda.command.Command;
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.interpreters.Virtualenv;
import jenkins.plugins.shiningpanda.matrix.PythonAxis;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import jenkins.plugins.shiningpanda.util.BuilderUtil;
import jenkins.plugins.shiningpanda.workspace.Workspace;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class VirtualenvBuilder extends Builder implements Serializable
{

    /**
     * Name of the PYTHON to invoke
     */
    public final String pythonName;

    /**
     * Home folder for this VIRTUALENV
     */
    public final String home;

    /**
     * Must be public else not available in Jelly
     */
    public boolean clear;

    /**
     * Must be public else not available in Jelly
     */
    public boolean useDistribute;

    /**
     * Must be public else not available in Jelly
     */
    public boolean noSitePackages;

    /**
     * The command to execute in the PYTHON environment
     */
    public final String command;

    /**
     * Do not consider the build as a failure if any of the commands exits with
     * a non-zero exit code.
     */
    public final boolean ignoreExitCode;

    /**
     * Constructor using fields
     * 
     * @param pythonName
     *            The name of the PYTHON to use to create the VIRTUALENV
     * @param home
     *            The home folder for this VIRTUALENV
     * @param clear
     *            Must the VIRTUALENV be cleared on each build?
     * @param useDistribute
     *            Choose between SETUPTOOLS and DISTRIBUTE
     * @param noSitePackages
     *            Do not include the contents of site-packages when creating the
     *            virtual environment
     * @param ignoreExitCode
     *            Do not consider the build as a failure if any of the commands
     *            exits with a non-zero exit code
     * @param command
     *            The command to execute
     */
    @DataBoundConstructor
    public VirtualenvBuilder(String pythonName, String home, boolean clear, boolean useDistribute, boolean noSitePackages,
            String command, boolean ignoreExitCode)
    {
        // Call super
        super();
        // Store the name of the PYTHON to invoke
        this.pythonName = pythonName;
        // Store VIRTUALENV home folder
        this.home = home;
        // If true reset the VIRTUALENV at each build
        this.clear = clear;
        // Use DISTRIBUTE instead of SETUPTOOLS
        this.useDistribute = useDistribute;
        // Hide the packages installed in the PYTHON creating the VIRTUALENV
        this.noSitePackages = noSitePackages;
        // Normalize and store the command
        this.command = command;
        // Store the ignore flag
        this.ignoreExitCode = ignoreExitCode;
    }

    /**
     * Get the home folder of the VIRTUALENV
     * 
     * @return The home folder
     */
    public String getHome()
    {
        return home;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException,
            IOException
    {
        // Get the workspace
        Workspace workspace = Workspace.fromHome(build.getWorkspace());
        // Get the environment variables for this build
        EnvVars environment = BuilderUtil.getEnvironment(build, listener);
        // Get the name of the PYTHON to use
        String name = BuilderUtil.isMatrix(build) ? environment.get(PythonAxis.KEY) : pythonName;
        // Expand the HOME folder with these variables
        PythonInstallation installation = PythonInstallation.fromName(name);
        //
        if (installation == null)
            return false;
        // Get the PYTHON
        Python interpreter = installation.forBuild(listener, environment).toInterpreter(launcher.getChannel());
        // Check if valid
        if (interpreter == null)
            return false;
        // Create VIRTUALENV
        Virtualenv virtualenv = new Virtualenv(launcher, getHome(), environment);

        if (clear || virtualenv.isOutdated(BuilderUtil.lastConfigure(build)))
            if (!virtualenv.create(launcher, listener, workspace, interpreter, useDistribute, noSitePackages))
                return false;

        environment.overrideAll(virtualenv.getEnvironment());
        // Set the environment of this specific builder
        return Command.get(workspace.isUnix(), command, ignoreExitCode).launch(launcher, listener, environment,
                workspace.getHome());
    }

    private static final long serialVersionUID = 1L;

    /**
     * Descriptor for this builder
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
    {

        /**
         * Let Jelly access the hosted flag.
         */
        public static boolean HOSTED = ShiningPanda.HOSTED;

        /**
         * Flag to determine if PYTHON selection is let to user (useful for
         * build matrix)
         */
        public volatile boolean showInstallations;

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName()
        {
            return Messages.VirtualenvBuilder_DisplayName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile()
        {
            return "/plugin/shiningpanda/help/VirtualenvBuilder/help.html";
        }

        /*
         * (non-Javadoc)
         * 
         * @see jenkins.plugins.shiningpanda.InstalledPythonBuildStepDescriptor#
         * isApplicable(java.lang.Class)
         */
        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType)
        {
            // Set the flag (dirty to do this here, but do not know where to do
            // it)
            showInstallations = !BuilderUtil.isMatrix(jobType);
            // If there's no PYTHON configured, there's no point in PYTHON
            // builders
            return !PythonInstallation.isEmpty();
        }

        /**
         * Checks if the VIRTUALENV home is valid
         * 
         * @param project
         *            The linked project, to check permissions
         * @param value
         *            The value to check
         * @return The validation result
         */
        public FormValidation doCheckHome(@SuppressWarnings("rawtypes") @AncestorInPath AbstractProject project,
                @QueryParameter File value)
        {
            // Check that path specified
            if (Util.fixEmptyAndTrim(value.getPath()) == null)
                return FormValidation.error(Messages.VirtualenvBuilder_HomeDirectoryRequired());
            // Check that path is relative in workspace
            File expanded = new File(Util.replaceMacro(value.getPath(), EnvVars.masterEnvVars));
            if (expanded.isAbsolute() || FilenameUtils.normalize(expanded.getPath()) == null)
                return FormValidation.error(Messages.VirtualenvBuilder_HomeNotRelative());
            // Do not need to check more as files are located on slaves
            return FormValidation.ok();
        }
    }
}
