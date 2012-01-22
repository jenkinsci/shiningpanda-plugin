/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2012 ShiningPanda S.A.S.
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
package jenkins.plugins.shiningpanda.interpreters;

import hudson.FilePath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jenkins.plugins.shiningpanda.utils.EnvVarsUtil;
import jenkins.plugins.shiningpanda.utils.FilePathUtil;

public class CPython extends Python
{

    /**
     * Constructor using fields
     * 
     * @param home
     *            The home folder
     * @throws InterruptedException
     * @throws IOException
     */
    protected CPython(FilePath home) throws IOException, InterruptedException
    {
        super(home);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.interpreters.Python#isCPython()
     */
    @Override
    public CPython isCPython()
    {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.interpreters.Python#getExecutable()
     */
    @Override
    public FilePath getExecutable() throws IOException, InterruptedException
    {
        // Check if on Windows
        if (isWindows())
            // If on Windows, look for python.exe
            return FilePathUtil.isFileOrNull(getHome().child("python.exe"));
        // If on UNIX, this can be PYTHON 3 or PYTHON 2
        return FilePathUtil.isFileOrNull(getHome().child("bin").child("python3"), getHome().child("bin").child("python"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.interpreters.Python#getEnvironment(boolean)
     */
    @Override
    public Map<String, String> getEnvironment(boolean includeHomeKey) throws IOException, InterruptedException
    {
        // Store the environment
        Map<String, String> environment = new HashMap<String, String>();
        // Check if home variable required
        if (includeHomeKey)
            // If required define PYTHONHOME
            environment.put("PYTHONHOME", getHome().getRemote());
        // Else delete it from environment
        else
            // Delete
            environment.put("PYTHONHOME", null);
        // Check if on Windows
        if (isWindows())
            // If on Windows add the home folder and the scripts folder in the
            // PATH
            environment.put("PATH+", getHome().getRemote() + ";" + getHome().child("Scripts").getRemote());
        // Handle UNIX case
        else
        {
            // Add the bin folder in the PATH
            environment.put("PATH+", getHome().child("bin").getRemote());
            // Add the library folder in the path to look for libraries
            environment.putAll(EnvVarsUtil.getLibs(getHome().child("lib")));
        }
        // Return the environment
        return environment;
    }

}
