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
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.remoting.Callable;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolProperty;
import hudson.tools.ToolInstallation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public abstract class PythonInstallation extends ToolInstallation implements EnvironmentSpecific<PythonInstallation>,
        NodeSpecific<PythonInstallation>
{

    /**
     * Name of the default installation
     */
    public final static String defaultInstallationName = "(Default)";

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
    public PythonInstallation(String name, String home, List<? extends ToolProperty<?>> properties)
    {
        super(name, home, properties);
    }

    /**
     * Get the PYTHON executable
     * 
     * @param launcher
     *            The task launcher
     * @return The path of the PYTHON executable
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("serial")
    public String getExecutable(Launcher launcher) throws IOException, InterruptedException
    {
        return launcher.getChannel().call(new Callable<String, IOException>()
        {
            public String call() throws IOException
            {
                File exe = getExeFile("python");
                if (exe.exists())
                    return exe.getPath();
                exe = getExeFile("python3");
                if (exe.exists())
                    return exe.getPath();
                return null;
            }
        });
    }

    /**
     * Check if this is a VIRTUALENV by checking the activation script
     * 
     * @param launcher
     *            The task launcher
     * @return Return true is this is a VIRTUALENV
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("serial")
    public boolean isVirtualenv(Launcher launcher) throws IOException, InterruptedException
    {
        return launcher.getChannel().call(new Callable<Boolean, IOException>()
        {
            public Boolean call() throws IOException
            {
                return new File(Util.replaceMacro(getHome(), EnvVars.masterEnvVars), "bin/activate").exists();
            }
        });
    }

    /**
     * Get the executable path for the provided executable name
     * 
     * @param execName
     *            The executable to find
     * @return Full path to executable if exists
     */
    protected File getExeFile(String execName)
    {
        if (File.separatorChar == '\\')
            execName += ".exe";
        String pythonHome = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
        return new File(pythonHome, "bin/" + execName);
    }

    /**
     * Get the variable matching the home folder of this installation:
     * PYTHONHOME, VIRTUAL_ENV...
     * 
     * @return The variable
     */
    public abstract String getHomeVar();

    /**
     * Set the environment for this installation
     * 
     * @param envVars
     *            The environment variable to update
     * @param pathSeparator
     *            The remote path separator
     * @throws IOException
     * @throws InterruptedException
     */
    public void setEnvironment(EnvVars envVars, String pathSeparator) throws IOException, InterruptedException
    {
        envVars.remove("PYTHONHOME");
        envVars.put(getHomeVar(), getHome());
        String binFolder = new File(getHome(), "bin").getPath();
        String pathValue = envVars.remove("PATH");
        envVars.put("PATH", pathValue == null ? binFolder : binFolder + pathSeparator + pathValue);
        String libFolder = new File(getHome(), "lib").getPath();
        List<String> libVars = new ArrayList<String>();
        libVars.add("LD_LIBRARY_PATH");
        libVars.add("DYLD_LIBRARY_PATH");
        libVars.add("LIBPATH");
        libVars.add("SHLIB_PATH");
        for (String libVar : libVars)
        {
            String libValue = envVars.remove(libVar);
            envVars.put(libVar, libValue == null ? libFolder : libFolder + pathSeparator + libValue);
        }
    }

    /**
     * Check if the installation is in workspace
     * 
     * @param ws
     *            The workspace file path
     * @return Return true if in workspace
     */
    public boolean isInWorkspace(FilePath ws)
    {
        return FilenameUtils.normalize(getHome()).startsWith(FilenameUtils.normalize(ws.getRemote()));
    }

    private static final long serialVersionUID = 1L;

}