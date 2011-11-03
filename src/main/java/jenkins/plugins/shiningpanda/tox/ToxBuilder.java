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
package jenkins.plugins.shiningpanda.tox;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.ShiningPandaUtil;
import jenkins.plugins.shiningpanda.StandardPythonInstallation;

import org.kohsuke.stapler.AncestorInPath;
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
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException
    {
        return perform(build, launcher, (TaskListener) listener);
    }

    /**
     * Perform the build
     * 
     * @param build
     *            The build
     * @param launcher
     *            The task launcher
     * @param listener
     *            The listener
     * @return Return true if the build went well
     * @throws InterruptedException
     */
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws InterruptedException
    {
        // Store the return code
        int r;
        // Catch errors to log them
        try
        {
            // Get the environment variables for this build
            EnvVars envVars = build.getEnvironment(listener);
            // Check that no PYTHONHOME entry
            if (envVars.containsKey("PYTHONHOME"))
            {
                // Log
                listener.fatalError(Messages.ToxBuilder_PythonHomeSet());
                // Fail the build
                return false;
            }
            // Add build variables, such as the user defined text axis for
            // matrix builds
            for (Map.Entry<String, String> e : build.getBuildVariables().entrySet())
                // Add the variable
                envVars.put(e.getKey(), e.getValue());
            // Set the environment of this specific builder
            StandardPythonInstallation toxInstallation = setEnvironment(envVars, Computer.currentComputer().getNode(),
                    launcher, listener);
            // Check if found an installation
            if (toxInstallation == null)
            {
                // Log
                listener.fatalError(Messages.ToxBuilder_NoToxExecutable());
                // No TOX executable, no need to go further
                return false;
            }
            // Delete PYTHONHOME, else TOX will fail
            envVars.remove("PYTHONHOME");
            // Start command line
            r = launcher.launch().cmds(buildCommandLine(toxInstallation.getToxExecutable(launcher))).envs(envVars)
                    .stdout(listener).pwd(build.getWorkspace()).join();
        }
        catch (IOException e)
        {
            // Log exception
            Util.displayIOException(e, listener);
            // Log error message
            e.printStackTrace(listener.fatalError(Messages.ToxBuilder_CommandFailed()));
            // Set exit code
            r = -1;
        }
        // Continue if exit code is valid
        return r == 0;
    }

    /**
     * Build the command line to call TOX
     * 
     * @param toxInstallation
     *            Absolute path to the TOX executable
     * 
     * @return The command line
     */
    public List<String> buildCommandLine(String toxExecutable)
    {
        // Initialize the command line
        List<String> args = new ArrayList<String>();
        // Add the TOX executable
        args.add(toxExecutable);
        // Add the configuration file option
        args.add("-c");
        // Add the configuration file
        args.add(toxIni);
        // Check if force recreation of virtual environments
        if (recreate)
            // If yes, add recreate option
            args.add("--recreate");
        // Return the command line
        return args;
    }

    /**
     * Set PYTHON environment
     * 
     * @param envVars
     *            The environment to update
     * @param node
     *            The node where we run on
     * @param launcher
     *            The task launcher
     * @param listener
     *            The listener
     * @return The PYTHON installation containing TOX
     * @throws InterruptedException
     * @throws IOException
     */
    protected StandardPythonInstallation setEnvironment(EnvVars envVars, Node node, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException
    {
        // Get the path separator
        String pathSeparator = ShiningPandaUtil.getPathSeparator(launcher);
        // Get the list of installations
        List<StandardPythonInstallation> pis = new ArrayList<StandardPythonInstallation>();
        // Go threw the PYTHON installations
        for (StandardPythonInstallation pi : ((DescriptorImpl) getDescriptor()).getInstallations())
            // Get the final installation
            pis.add(pi.forNode(node, listener).forEnvironment(envVars));
        // Get the TOX installation
        StandardPythonInstallation toxInstallation = getToxInstallation(launcher, pis);
        // Check if exists
        if (toxInstallation != null)
        {
            // The first installation should come first in environment variables
            Collections.reverse(pis);
            // Go threw the PYTHON installations
            for (StandardPythonInstallation pi : pis)
            {
                // Check that is not the installation with TOX
                if (pi != toxInstallation)
                    // Set the environment
                    pi.setEnvironment(envVars, pathSeparator);
            }
            // Set TOX installation environments first
            toxInstallation.setEnvironment(envVars, pathSeparator);
        }
        // Return the TOX executable
        return toxInstallation;
    }

    /**
     * Get the PYTHON installation with a TOX installation
     * 
     * @param launcher
     *            The launcher
     * @param installations
     *            The PYTHON installation
     * @return The PYTHON installation
     * @throws InterruptedException
     * @throws IOException
     */
    private StandardPythonInstallation getToxInstallation(Launcher launcher, List<StandardPythonInstallation> installations)
            throws IOException, InterruptedException
    {
        // Go threw the installations
        for (StandardPythonInstallation installation : installations)
        {
            // Check if there is a TOX executable
            if (installation.getToxExecutable(launcher) != null)
                // If yes, we found the installation
                return installation;
        }
        // Installation not found
        return null;

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
            return "/plugin/shiningpanda/help/ToxBuilder/help.html";
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
            return getInstallations().length != 0 && jobType.equals(MatrixProject.class);
        }

        /**
         * Get all the PYTHON installations
         * 
         * @return An array of PYTHON installations
         */
        public StandardPythonInstallation[] getInstallations()
        {
            return ToolInstallation.all().get(StandardPythonInstallation.DescriptorImpl.class).getInstallations();
        }

        /**
         * Checks if the TOX configuration file is specified
         * 
         * @param project
         *            The linked project, to check permissions
         * @param value
         *            The value to check
         * @return The validation result
         */
        public FormValidation doCheckToxIni(@SuppressWarnings("rawtypes") @AncestorInPath AbstractProject project,
                @QueryParameter File value)
        {
            // Check that path is specified
            if (Util.fixEmptyAndTrim(value.getPath()) == null)
                return FormValidation.error(Messages.ToxBuilder_ToxIniRequired());
            // Do not need to check more as files are located on slaves
            return FormValidation.ok();
        }
    }
}
