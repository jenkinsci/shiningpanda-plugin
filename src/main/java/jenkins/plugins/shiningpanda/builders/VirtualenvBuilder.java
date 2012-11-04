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
package jenkins.plugins.shiningpanda.builders;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.command.CommandNature;
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.interpreters.Virtualenv;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import jenkins.plugins.shiningpanda.utils.BuilderUtil;
import jenkins.plugins.shiningpanda.workspace.Workspace;

import org.kohsuke.stapler.DataBoundConstructor;

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
     * Clear out the non-root install and start from scratch
     */
    public boolean clear;

    /**
     * Use Distribute instead of SETUPTOOLS
     */
    public boolean useDistribute;

    /**
     * Don't give access to the global site-packages
     * 
     * @deprecated since 0.6
     */
    @Deprecated
    private transient Boolean noSitePackages;

    /**
     * Give access to the global site-packages
     */
    public boolean systemSitePackages;

    /**
     * The nature of the command: PYTHON, shell, X shell
     */
    public final String nature;

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
     * @param systemSitePackages
     *            Give access to the global site-packages directory to the
     *            virtual environment
     * @param nature
     *            The nature of the command: PYTHON, shell, X shell
     * @param command
     *            The command to execute
     * @param ignoreExitCode
     *            Do not consider the build as a failure if any of the commands
     *            exits with a non-zero exit code
     */
    @DataBoundConstructor
    public VirtualenvBuilder(String pythonName, String home, boolean clear, boolean useDistribute, boolean systemSitePackages,
            String nature, String command, boolean ignoreExitCode)
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
        // Give access to the global site-packages directory
        this.systemSitePackages = systemSitePackages;
        // Store the nature of the command
        this.nature = nature;
        // Normalize and store the command
        this.command = command;
        // Store the ignore flag
        this.ignoreExitCode = ignoreExitCode;
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
        Workspace workspace = Workspace.fromBuild(build);
        // Get the environment variables for this build
        EnvVars environment = BuilderUtil.getEnvironment(build, listener);
        // Check if this is a valid environment
        if (environment == null)
            // Invalid, no need to go further
            return false;
        // Get the PYTHON installation to use
        PythonInstallation installation = BuilderUtil.getInstallation(build, listener, environment, pythonName);
        // Check if an installation was found
        if (installation == null)
            // If not installation found, do not continue the build
            return false;
        // Get the PYTHON
        Python interpreter = BuilderUtil.getInterpreter(launcher, listener, installation.getHome());
        // Check if found an interpreter
        if (interpreter == null)
            // If no interpreter found, no need to continue
            return false;
        // Create a VIRTUALENV
        Virtualenv virtualenv = BuilderUtil.getVirtualenv(listener, workspace.getVirtualenvHome(home));
        // Check if this is a valid VIRTUALENV
        if (virtualenv == null)
            // Invalid VIRTUALENV, do not continue
            return false;
        // Get the working directory
        FilePath pwd = build.getWorkspace();
        // Check if clean required or if configuration changed
        if (clear || virtualenv.isOutdated(workspace, interpreter, useDistribute, systemSitePackages))
            // A new environment is required
            if (!virtualenv.create(launcher, listener, workspace, pwd, environment, interpreter, useDistribute,
                    systemSitePackages))
                // Failed to create the environment, do not continue
                return false;
        // Launch script
        return BuilderUtil.launch(launcher, listener, pwd, environment, virtualenv, nature, command, ignoreExitCode);
    }

    /**
     * ShiningPanda 0.5 to 0.6 compatibility: VIRTUALENV option
     * --no-site-packages replaced by --system-site-packages
     * 
     * @return The builder
     */
    private Object readResolve()
    {
        // Check if old flag defined
        if (noSitePackages != null)
            // If defined, set the new one at the opposite value
            systemSitePackages = !noSitePackages;
        // Return the builder
        return this;
    }

    private static final long serialVersionUID = 1L;

    /**
     * Descriptor for this builder
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
    {
        /**
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
            return Functions.getResourcePath() + "/plugin/shiningpanda/help/builders/VirtualenvBuilder/help.html";
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
            // If there's no PYTHON configured, there's no point in PYTHON
            // builders
            return !PythonInstallation.isEmpty();
        }

        /**
         * Check if this is a matrix project.
         * 
         * @return true if this is a matrix project.
         */
        public boolean isMatrix(Object it)
        {
            return it instanceof MatrixProject;
        }

        /**
         * Get the PYTHON installations.
         * 
         * @return The list of installations
         */
        public PythonInstallation[] getInstallations()
        {
            // Delegate
            return PythonInstallation.list();
        }

        /**
         * Get the list of the available command natures.
         * 
         * @return The list of natures
         */
        public List<CommandNature> getNatures()
        {
            return CommandNature.ALL;
        }
    }
}
