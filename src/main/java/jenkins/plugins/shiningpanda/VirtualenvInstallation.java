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
import hudson.model.Node;
import hudson.tools.ToolProperty;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class VirtualenvInstallation extends PythonInstallation
{

    /**
     * Constructor using field.
     * 
     * @param home
     *            The home folder for this VIRTUALENV installation.
     */
    public VirtualenvInstallation(String home)
    {
        this("virtualenv", home, Collections.<ToolProperty<?>> emptyList());
    }

    /**
     * Constructor using fields
     * 
     * @param name
     *            The name of the VIRTUALENV
     * @param home
     *            The home folder for this VIRTUALENV installation
     * @param properties
     *            The properties
     */
    protected VirtualenvInstallation(String name, String home, List<? extends ToolProperty<?>> properties)
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
        return "VIRTUAL_ENV";
    }

    private static final long serialVersionUID = 1L;

    /**
     * Get the installation for the environment
     * 
     * @param environment
     *            The environment
     * @return The new installation
     */
    public VirtualenvInstallation forEnvironment(EnvVars environment)
    {
        return new VirtualenvInstallation(getName(), environment.expand(getHome()), getProperties().toList());
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
    public VirtualenvInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException
    {
        return new VirtualenvInstallation(getName(), translateFor(node, log), getProperties().toList());
    }

}
