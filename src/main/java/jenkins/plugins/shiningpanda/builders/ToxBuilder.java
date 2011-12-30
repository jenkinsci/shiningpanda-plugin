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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.interpreters.Virtualenv;
import jenkins.plugins.shiningpanda.matrix.ToxAxis;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import jenkins.plugins.shiningpanda.util.BuilderUtil;
import jenkins.plugins.shiningpanda.workspace.Workspace;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class ToxBuilder extends Builder implements Serializable
{

    /**
     * Path to the tox.ini file
     */
    public final String toxIni;

    /**
     * Force recreation of virtual environments
     */
    public final boolean recreate;

    @DataBoundConstructor
    public ToxBuilder(String toxIni, boolean recreate)
    {
        // Call super
        super();
        // Store the path to the tox.ini file
        this.toxIni = Util.fixEmptyAndTrim(toxIni);
        // Store the recreation flag
        this.recreate = recreate;
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
        // Check if environment contains a TOX axis
        if (!environment.containsKey(ToxAxis.KEY))
        {
            // Log
            listener.fatalError(Messages.ToxBuilder_ToxAxis_Required());
            // No need to go further
            return false;
        }
        // Get a VIRTUALENV to install TOX
        Virtualenv virtualenv = BuilderUtil.getVirtualenv(listener, workspace.getVirtualenvHome());
        // Check if is a valid one
        if (virtualenv == null)
            // Invalid, no need to go further
            return false;
        // Check if out of date to be able to create a new one
        if (virtualenv.isOutdated(BuilderUtil.lastConfigure(build)))
        {
            // Get an interpreter to be able to create the VIRTUALENV
            Python interpreter = BuilderUtil.getInterpreter(launcher, listener, environment);
            // Check if found one
            if (interpreter == null)
                // No interpreter found, no need to continue
                return false;
            // Create the VIRTUALENV
            if (!virtualenv.create(launcher, listener, environment, workspace, interpreter, true, true))
                // Failed to create the VIRTUALENV, do not continue
                return false;
        }
        // Install or upgrade TOX
        if (!virtualenv.pipInstall(launcher, listener, environment, workspace, "tox"))
            // Failed to install TOX, do not continue
            return false;
        // Get all the available interpreters on the executor
        List<Python> interpreters = BuilderUtil.getInterpreters(launcher, listener, environment);
        // Reverse the order to be able to sort the environment variables
        Collections.reverse(interpreters);
        // Go threw the interpreters to add them in the path
        for (Python interpreter : interpreters)
            // Add the environment without the home variables
            environment.overrideAll(interpreter.getEnvironment(false));
        // Launch TOX
        return virtualenv.tox(launcher, listener, environment, workspace, toxIni, recreate);
    }

    private static final long serialVersionUID = 1L;

    /**
     * Descriptor for this builder
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
    {
        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName()
        {
            return Messages.ToxBuilder_DisplayName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile()
        {
            return "/plugin/shiningpanda/help/builders/ToxBuilder/help.html";
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
         */
        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType)
        {
            // Only available in matrix projects if some installations exist
            return !PythonInstallation.isEmpty() && BuilderUtil.isMatrix(jobType);
        }

        /**
         * Checks if the TOX configuration file is specified.
         * 
         * @param value
         *            The value to check
         * @return The validation result
         */
        public FormValidation doCheckToxIni(@QueryParameter String value)
        {
            // Check that path is specified
            if (Util.fixEmptyAndTrim(value) == null)
                // A tox.ini file is required
                return FormValidation.error(Messages.ToxBuilder_ToxIni_Required());
            // Do not need to check more as files are located on slaves
            return FormValidation.ok();
        }
    }
}
