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
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.util.BuilderUtil;
import jenkins.plugins.shiningpanda.util.FormValidationUtil;
import jenkins.plugins.shiningpanda.workspace.Workspace;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class CustomPythonBuilder extends Builder implements Serializable
{

    /**
     * Home directory for the VIRTUALENV
     */
    public final String home;

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
     * @param home
     *            The home directory for VIRTUALENV
     * @param ignoreExitCode
     *            Do not consider the build as a failure if any of the commands
     *            exits with a non-zero exit code
     * @param command
     *            The command to execute
     */
    @DataBoundConstructor
    public CustomPythonBuilder(String home, String command, boolean ignoreExitCode)
    {
        // Call super
        super();
        // Store the home directory
        this.home = home;
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
        Workspace workspace = Workspace.fromHome(build.getWorkspace());
        // Get the environment variables for this build
        EnvVars environment = BuilderUtil.getEnvironment(build, listener);
        // Get the interpreter
        Python interpreter = BuilderUtil.getInterpreter(launcher, listener, environment.expand(home));
        // Check if got an interpreter
        if (interpreter == null)
            // Failed to get the interpreter, no need to go further
            return false;
        // Launch script
        return BuilderUtil.launch(launcher, listener, environment, workspace, interpreter, command, ignoreExitCode);
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
            return Messages.CustomVirtualenvBuilder_DisplayName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile()
        {
            return "/plugin/shiningpanda/help/CustomVirtualenvBuilder/help.html";
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
         */
        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType)
        {
            return true;
        }

        /**
         * Check if the VIRTUALENV home is valid
         * 
         * @param project
         *            The project for this builder
         * @param value
         *            The folder to check
         * @return The result of the validation
         */
        public FormValidation doCheckHome(@SuppressWarnings("rawtypes") @AncestorInPath AbstractProject project,
                @QueryParameter File value)
        {
            // This can be used to check the existence of a file on the
            // server, so needs to be protected
            if (!project.hasPermission(Item.CONFIGURE))
                return FormValidation.ok();
            // Validate PYTHON home
            return FormValidationUtil.validatePythonHome(value);
        }
    }
}
