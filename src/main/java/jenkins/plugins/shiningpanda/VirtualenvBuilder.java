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
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class VirtualenvBuilder extends InstalledPythonBuilder
{

    /**
     * Home folder for this VIRTUALENV
     */
    protected final String home;

    /**
     * Must be public else not available in Jelly
     */
    public boolean clear;

    /**
     * Must be public else not available in Jelly
     */
    public boolean useDistribute;

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
     * @param command
     *            The command to execute
     */
    @DataBoundConstructor
    public VirtualenvBuilder(String pythonName, String home, boolean clear, boolean useDistribute, String command)
    {
        // Call super
        super(pythonName, command);
        // Store fields
        this.home = home;
        this.clear = clear;
        this.useDistribute = useDistribute;
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

    /**
     * Get the remote home by expanding its variables
     * 
     * @param launcher
     *            The launcher
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("serial")
    public String getRemoteHome(Launcher launcher) throws IOException, InterruptedException
    {
        return launcher.getChannel().call(new Callable<String, IOException>()
        {
            public String call() throws IOException
            {
                return Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
            }
        });
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
            TaskListener listener) throws IOException, InterruptedException
    {
        // Get the workspace path
        FilePath ws = build.getWorkspace();
        // Get the PYTHON to create this VIRTUALENV
        StandardPythonInstallation pi = getPython(build, node, listener, envVars);
        // If got a PYTHON, check that executable exists and is not already a
        // VIRTUALENV
        if (pi != null)
        {
            // Validate PYTHONHOME
            if (!ShiningPandaUtil.validatePythonHome(pi, listener))
                // Can't go further as PYTHONHOME is not valid
                return false;
            // Check that executable exists
            String pythonExe = pi.getExecutable(launcher);
            if (pythonExe == null)
            {
                listener.fatalError(Messages.VirtualenvBuilder_NoPythonExecutable(pi.getHome()));
                return false;
            }
            // Check that not a VIRTUALENV
            if (pi.isVirtualenv(launcher))
            {
                listener.fatalError(Messages.VirtualenvBuilder_AlreadyAVirtualenv(pi.getHome()));
                return false;
            }
        }
        // Check if must clean the folder
        FilePath virtualenv = new FilePath(ws, getRemoteHome(launcher));
        // Get the VIRTUALENV
        VirtualenvInstallation vi = getVirtualenv(virtualenv.getRemote(), build, node, listener, envVars);
        // Validate installation
        if (!ShiningPandaUtil.validatePythonHome(vi, listener))
            // Can't go further
            return false;
        // Check that in workspace
        if (!vi.isInWorkspace(ws))
        {
            // Log
            listener.fatalError(Messages.VirtualenvBuilder_NotInWorkspace(vi.getHome(), ws.getRemote()));
            // Can't go further
            return false;
        }
        // Get the path separator
        String pathSeparator = getPathSeparator(launcher);
        // Get the time stamp file in VIRTUALENV
        FilePath timestamp = new FilePath(virtualenv, ".timestamp");
        // time stamp
        boolean outdated = timestamp.lastModified() < build.getParent().getConfigFile().getFile().lastModified();
        // Clean folder if required
        if (virtualenv.exists() && (clear || outdated))
            // Delete the VIRTUALENV folder
            virtualenv.deleteRecursive();
        // Flag that show if the VIRTUALENV was created
        boolean created = false;
        // If not exists, create a new VIRTUALENV
        if (!virtualenv.exists())
        {
            // Update flag
            created = true;
            // Collect command line arguments for creation
            List<String> args = new ArrayList<String>();
            // If no PYTHON installation specified, use simple executable name
            String virtualenvExe = "virtualenv";
            // Check that VIRTUALENV executable exists if PYTHON installation
            // provided
            if (pi != null)
            {
                virtualenvExe = pi.getVirtualenvExecutable(launcher);
                if (virtualenvExe == null)
                {
                    listener.fatalError(Messages.VirtualenvBuilder_NoVirtualenvExecutable(pi.getHome()));
                    return false;
                }
            }
            // Add call to VIRTUALENV
            args.add(virtualenvExe);
            // Add distribute option
            if (useDistribute)
                args.add("--distribute");
            // Path to the VIRTUALENV
            args.add(virtualenv.getRemote());
            // Get the execution environment for the VIRTUALENV creation
            EnvVars piEnvVars = build.getEnvironment(listener);
            if (pi != null)
                pi.setEnvironment(piEnvVars, pathSeparator);
            // Start creation and check successful
            int returnCode = launcher.launch().cmds(args).envs(piEnvVars).stdout(listener).pwd(build.getWorkspace()).join();
            if (returnCode != 0)
            {
                listener.fatalError(Messages.VirtualenvBuilder_VirtualenvFailed(returnCode));
                return false;
            }
        }
        // Check that VIRTUALENV executable exists
        String virtualenvPythonExe = vi.getExecutable(launcher);
        if (virtualenvPythonExe == null)
        {
            listener.fatalError(Messages.VirtualenvBuilder_NoVirtualenvPythonExecutable(vi.getHome()));
            return false;
        }
        // Set time stamp if just created (wanted to check the executable
        // existence)
        if (created)
            timestamp.touch(System.currentTimeMillis());
        // Set the build environment
        vi.setEnvironment(envVars, pathSeparator);
        // Success
        return true;
    }

    private static final long serialVersionUID = 1L;

    /**
     * Descriptor for this builder
     */
    @Extension
    public static final class DescriptorImpl extends InstalledPythonBuildStepDescriptor
    {
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
            // This can be used to check the existence of a file on the
            // server, so needs to be protected
            if (!project.hasPermission(Item.CONFIGURE))
                return FormValidation.ok();
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
