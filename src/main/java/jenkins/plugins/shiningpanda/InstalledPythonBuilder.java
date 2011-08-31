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
    protected final String pythonName;

    /**
     * Constructor using fields
     * 
     * @param pythonName
     *            The name of the PYTHON to invoke
     * @param command
     *            The command to execute in this PYTHON environment
     */
    public InstalledPythonBuilder(String pythonName, String command)
    {
        // Call super
        super(command);
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
        for (StandardPythonInstallation pi : getDescriptor().getInstallations())
        {
            if (name != null && name.equals(pi.getName()))
                return pi;
        }
        return null;
    }

    /**
     * Get the PYTHON to invoke, or null if not found
     * 
     * @return The PYTHON installation
     */
    public StandardPythonInstallation getPython()
    {
        return getPython(pythonName);
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
        // Get PYTHON
        StandardPythonInstallation pi = getPython();
        // If unable to get it, check if variables if name is provided by matrix
        // project
        if (pi == null)
            pi = getPython(build.getBuildVariables().get(PythonAxis.KEY));
        // If still no PYTHON, get the default one (the first in the list)
        if (pi == null)
        {
            StandardPythonInstallation[] pis = getDescriptor().getInstallations();
            if (pis.length != 0)
                pi = pis[0];
        }
        // Can be still null if no PYTHON registered
        if (pi != null)
        {
            pi = pi.forNode(node, listener);
            pi = pi.forEnvironment(envVars);
        }
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
        return (InstalledPythonBuildStepDescriptor) super.getDescriptor();
    }

    private static final long serialVersionUID = 1L;

}
