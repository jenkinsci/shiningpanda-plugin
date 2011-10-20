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
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Node;

import java.io.IOException;

public abstract class InstalledPythonBuilder extends PythonBuilder
{

    /**
     * Name of the PYTHON to invoke
     */
    public final String pythonName;

    /**
     * Constructor using fields
     * 
     * @param pythonName
     *            The name of the PYTHON to invoke
     * @param ignoreExitCode
     *            Do not consider the build as a failure if any of the commands
     *            exits with a non-zero exit code
     * @param command
     *            The command to execute in this PYTHON environment
     */
    public InstalledPythonBuilder(String pythonName, boolean ignoreExitCode, String command)
    {
        // Call super
        super(ignoreExitCode, command);
        // Store the name of the PYTHON to invoke
        this.pythonName = pythonName;
    }

    /**
     * Get a PYTHON given its name.
     * 
     * @param name
     *            The name of the PYTHON
     * @return The PYTHON installation
     */
    public StandardPythonInstallation getPython(String name)
    {
        // Go threw the installations
        for (StandardPythonInstallation pi : getDescriptor().getInstallations())
        {
            // Check if the name match the current installation name
            if (name != null && name.equals(pi.getName()))
                // If yes, we found it
                return pi;
        }
        // No installation matching the provided name
        return null;
    }

    /**
     * Get the PYTHON to invoke for the provided build (handle matrix
     * configuration)
     * 
     * @param build
     *            The build
     * @param node
     *            The node to run on
     * @param listener
     *            The task listener
     * @param envVars
     *            The environment variables
     * @return The PYTHON installation
     * @throws InterruptedException
     * @throws IOException
     */
    public StandardPythonInstallation getPython(AbstractBuild<?, ?> build, Node node, TaskListener listener, EnvVars envVars)
            throws InterruptedException, IOException
    {
        // Get the PYTHON name
        String name = pythonName;
        // Get PYTHON
        StandardPythonInstallation pi = getPython(name);
        // If unable to get it, check if variables if name is provided by matrix
        // project
        if (pi == null && build.getBuildVariables().containsKey(PythonAxis.KEY))
        {
            // Get the PYTHON name provided by the AXIS
            name = build.getBuildVariables().get(PythonAxis.KEY);
            // Get the PYTHON matching the AXIS
            pi = getPython(name);
        }
        // If still no PYTHON, get the default one (the first in the list)
        if (pi == null && name != null)
        {
            // Get the list of installations
            StandardPythonInstallation[] pis = getDescriptor().getInstallations();
            // Check if at least one installation
            if (pis.length != 0)
            {
                // Get the first one
                pi = pis[0];
                // Log that not using the defined one
                listener.error(Messages.InstalledPythonBuilder_InstallationNotFound(name, pi.getName()));
            }
        }
        // Can be still null if no PYTHON registered
        if (pi != null)
        {
            // Configure for the node
            pi = pi.forNode(node, listener);
            // Configure for the environment
            pi = pi.forEnvironment(envVars);
        }
        // Return the installation
        return pi;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hudson.tasks.Builder#getDescriptor()
     */
    @Override
    public InstalledPythonBuildStepDescriptor getDescriptor()
    {
        // Return the descriptor
        return (InstalledPythonBuildStepDescriptor) super.getDescriptor();
    }

    private static final long serialVersionUID = 1L;

}
