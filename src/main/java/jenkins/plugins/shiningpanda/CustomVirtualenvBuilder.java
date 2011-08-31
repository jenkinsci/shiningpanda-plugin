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
package jenkins.plugins.shiningpanda;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class CustomVirtualenvBuilder extends PythonBuilder
{

    /**
     * Home directory for the VIRTUALENV
     */
    private final String home;

    /**
     * Constructor using fields
     * 
     * @param home
     *            The home directory for VIRTUALENV
     * @param command
     *            The command to execute
     */
    @DataBoundConstructor
    public CustomVirtualenvBuilder(String home, String command)
    {
        // Call super
        super(command);
        // Store the home directory
        this.home = home;
    }

    /**
     * Get the home directory for this VIRTUALENV
     * 
     * @return The home directory
     */
    public String getHome()
    {
        return home;
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
            // Check that path specified
            if (Util.fixEmptyAndTrim(value.getPath()) == null)
                return FormValidation.error(Messages.CustomVirtualenvBuilder_HomeDirectoryRequired());
            // Do not need to check more as files are located on slaves
            return FormValidation.ok();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.shiningpanda.jenkins.python.PythonBuilder#setEnvironment(hudson.EnvVars
     * , hudson.model.AbstractBuild, hudson.model.Node, hudson.Launcher,
     * hudson.model.TaskListener)
     */
    @Override
    protected boolean setEnvironment(EnvVars envVars, AbstractBuild<?, ?> build, Node node, Launcher launcher,
            TaskListener listener) throws InterruptedException, IOException
    {
        // Create a new virtual environment and set the environment
        getVirtualenv(getHome(), build, node, listener, envVars).setEnvironment(envVars, getPathSeparator(launcher));
        // Environment ready
        return true;
    }
}
