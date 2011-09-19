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

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public final class StandardPythonInstallation extends PythonInstallation
{

    /**
     * Constructor using fields
     * 
     * @param name
     *            The name of the PYTHON
     * @param home
     *            The home folder for this PYTHON
     * @param properties
     *            The properties
     */
    @DataBoundConstructor
    public StandardPythonInstallation(String name, String home, List<? extends ToolProperty<?>> properties)
    {
        super(name, home, properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.shiningpanda.jenkins.python.PythonInstallation#getHomeVar()
     */
    @Override
    public String getHomeVar()
    {
        return "PYTHONHOME";
    }

    /**
     * Get the VIRTUALENV executable in this PYTHON installation
     * 
     * @param launcher
     *            The task launcher
     * @return The full path to the executable if exists
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("serial")
    public String getVirtualenvExecutable(Launcher launcher) throws IOException, InterruptedException
    {
        return launcher.getChannel().call(new Callable<String, IOException>()
        {
            public String call() throws IOException
            {
                File exe = getExeFile("virtualenv");
                if (exe.exists())
                    return exe.getPath();
                return null;
            }
        });
    }

    private static final long serialVersionUID = 1L;

    /**
     * Get the installation for the environment
     * 
     * @param environment
     *            The environment
     * @return The new installation
     */
    public StandardPythonInstallation forEnvironment(EnvVars environment)
    {
        return new StandardPythonInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    /**
     * Get the installation for the provided node
     * 
     * @param node
     *            The node
     * @param log
     *            The listener
     * @return The new installation
     */
    public StandardPythonInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException
    {
        return new StandardPythonInstallation(getName(), translateFor(node, log), getProperties().toList());
    }

    /**
     * Descriptor for this builder
     */
    @Extension
    public static class DescriptorImpl extends ToolDescriptor<StandardPythonInstallation>
    {

        /**
         * All the installations
         */
        @CopyOnWrite
        private volatile StandardPythonInstallation[] installations = new StandardPythonInstallation[0];

        /**
         * Default constructor
         */
        public DescriptorImpl()
        {
            // Load saved data on disk
            load();
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile()
        {
            return "/plugin/shiningpanda/help/StandardPythonInstallation/help.html";
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName()
        {
            return Messages.StandardPythonInstallation_DisplayName();
        }

        /**
         * Get all the installations
         * 
         * @return An array of installation
         */
        public StandardPythonInstallation[] getInstallations()
        {
            return installations;
        }

        /**
         * Set the installations
         */
        public void setInstallations(StandardPythonInstallation... installations)
        {
            this.installations = installations;
            save();
        }

        /**
         * Checks if the PYTHONHOME is valid
         * 
         * @param The
         *            value to check
         */
        public FormValidation doCheckHome(@QueryParameter File value)
        {
            // This can be used to check the existence of a file on the
            // server, so needs to be protected
            if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER))
                return FormValidation.ok();
            // Validate PYTHON home
            return ShiningPandaUtil.validatePythonHome(value);
        }

        /**
         * Check that the PYTHON name is specified
         * 
         * @param The
         *            value to check
         */
        public FormValidation doCheckName(@QueryParameter String value)
        {
            // Check that name specified
            if (Util.fixEmptyAndTrim(value) == null)
                return FormValidation.error(Messages.StandardPythonInstallation_NameRequired());
            // Check that not reserved
            if (PythonInstallation.defaultInstallationName.equals(value))
                return FormValidation.error(Messages.StandardPythonInstallation_NameReserved());
            // Successfully validated
            return FormValidation.ok();
        }
    }
}