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
package jenkins.plugins.shiningpanda.tools;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolProperty;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.util.FormValidationUtil;
import jenkins.plugins.shiningpanda.util.StringUtil;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class PythonInstallation extends ToolInstallation implements EnvironmentSpecific<PythonInstallation>,
        NodeSpecific<PythonInstallation>
{

    /**
     * Constructor using fields.
     * 
     * @param name
     *            The name of the PYTHON
     * @param home
     *            The home folder for this PYTHON
     * @param properties
     *            The properties
     */
    @DataBoundConstructor
    public PythonInstallation(String name, String home, List<? extends ToolProperty<?>> properties)
    {
        super(name, home, properties);
    }

    private static final long serialVersionUID = 1L;

    /**
     * Get the installation for the environment.
     * 
     * @param environment
     *            The environment
     * @return The new installation
     */
    public PythonInstallation forEnvironment(EnvVars environment)
    {
        return new PythonInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    /**
     * Get the installation for the provided node.
     * 
     * @param node
     *            The node
     * @param log
     *            The listener
     * @return The new installation
     * @throws IOException
     * @throws InterruptedException
     */
    public PythonInstallation forNode(Node node, TaskListener listener) throws IOException, InterruptedException
    {
        return new PythonInstallation(getName(), translateFor(node, listener), getProperties().toList());
    }

    /**
     * Get the installation for the provided build.
     * 
     * @param listener
     *            The build listener
     * @param environment
     *            The environment
     * @return The new installation
     * @throws IOException
     * @throws InterruptedException
     */
    public PythonInstallation forBuild(TaskListener listener, EnvVars environment) throws IOException, InterruptedException
    {
        return forNode(Computer.currentComputer().getNode(), listener).forEnvironment(environment);
    }

    /**
     * Get the related interpreter.
     * 
     * @param channel
     *            The channel
     * @return The interpreter if exists, else null
     * @throws IOException
     * @throws InterruptedException
     */
    public Python toInterpreter(VirtualChannel channel) throws IOException, InterruptedException
    {
        return Python.fromInstallation(channel, this);
    }

    /**
     * Get the related interpreter.
     * 
     * @param launcher
     *            The launcher
     * @return The interpreter if exists, else null
     * @throws IOException
     * @throws InterruptedException
     */
    public Python toInterpreter(Launcher launcher) throws IOException, InterruptedException
    {
        return toInterpreter(launcher.getChannel());
    }

    /**
     * Get the first existing PYTHON interpreter
     * 
     * @param channel
     *            The channel
     * @param listener
     *            The build listener
     * @param environment
     *            The environment
     * @return The PYTHON interpreter or null if not exist
     * @throws IOException
     * @throws InterruptedException
     */
    public static Python getInterpreter(VirtualChannel channel, TaskListener listener, EnvVars environment) throws IOException,
            InterruptedException
    {
        // Get the list of existing interpreter
        List<Python> interpreters = getInterpreters(channel, listener, environment);
        // Get the first one or return null
        return interpreters.isEmpty() ? null : interpreters.get(0);
    }

    /**
     * Get the list of existing interpreters.
     * 
     * @param channel
     *            The channel
     * @param listener
     *            The build listener
     * @param environment
     *            The environment
     * @return The list of PYTHON interpreters
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<Python> getInterpreters(VirtualChannel channel, TaskListener listener, EnvVars environment)
            throws IOException, InterruptedException
    {
        // Create the interpreter list
        List<Python> interpreters = new ArrayList<Python>();
        // Go threw all PYTHON installations
        for (PythonInstallation installation : list())
        {
            // Convert the installation for the current executor and get the
            // related interpreter
            Python interpreter = installation.forBuild(listener, environment).toInterpreter(channel);
            // Check if the interpreter exists. If it is, add it to the list
            if (interpreter != null)
                interpreters.add(interpreter);
        }
        // Return the list of interpreters
        return interpreters;
    }

    /**
     * Check if at least one installation is defined.
     * 
     * @return true if at least one installation is defined, else false
     */
    public static boolean isEmpty()
    {
        return list().length == 0;
    }

    /**
     * Get the installations.
     * 
     * @return The installations
     */
    public static PythonInstallation[] list()
    {
        return ToolInstallation.all().get(DescriptorImpl.class).getInstallations();
    }

    /**
     * Get the installation from its name.
     * 
     * @param name
     *            The name of the installation
     * @return The installation if found, else null
     */
    public static PythonInstallation fromName(String name)
    {
        // Go threw the installations
        for (PythonInstallation installation : list())
        {
            // Check if the name match the current installation name
            if (name != null && name.equals(installation.getName()))
                // If yes, we found it
                return installation;
        }
        // No installation matching the provided name
        return null;
    }

    /**
     * Installation descriptor
     */
    @Extension
    public static class DescriptorImpl extends ToolDescriptor<PythonInstallation>
    {

        /**
         * All installations
         */
        @CopyOnWrite
        private volatile PythonInstallation[] installations = new PythonInstallation[0];

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

        /*
         * (non-Javadoc)
         * 
         * @see hudson.tools.ToolDescriptor#getInstallations()
         */
        public PythonInstallation[] getInstallations()
        {
            return installations;
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.tools.ToolDescriptor#setInstallations(T[])
         */
        public void setInstallations(PythonInstallation... installations)
        {
            // Store installations
            this.installations = installations;
            // Save on disk
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
            return FormValidationUtil.validatePythonHome(value);
        }

        /**
         * Check that the PYTHON name is specified
         * 
         * @param The
         *            value to check
         */
        public FormValidation doCheckName(@QueryParameter String value)
        {
            // Trim name
            String fixName = Util.fixEmptyAndTrim(value);
            // Check that folder specified
            if (fixName == null)
                return FormValidation.error(Messages.ShiningPandaUtil_PythonNameRequired());
            // Check that path does not contains some whitespace chars
            if (StringUtil.hasWhitespace(fixName))
                return FormValidation.error(Messages.ShiningPandaUtil_PythonNameHasWhitespace(fixName));
            // Seems fine
            return FormValidation.ok();
        }
    }

}