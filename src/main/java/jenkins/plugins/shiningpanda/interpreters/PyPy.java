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
package jenkins.plugins.shiningpanda.interpreters;

import hudson.FilePath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jenkins.plugins.shiningpanda.util.FilePathUtil;

public class PyPy extends Python
{

    /**
     * Constructor using fields
     * 
     * @param home
     *            The home folder
     * @throws InterruptedException
     * @throws IOException
     */
    protected PyPy(FilePath home) throws IOException, InterruptedException
    {
        super(home);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.interpreters.Python#isPyPy()
     */
    @Override
    public PyPy isPyPy()
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
            // If on windows look for executables in home folder
            return FilePathUtil.isFileOrNull(join("pypy-c.exe"), join("pypy.exe"));
        // Else look in bin folder
        return FilePathUtil.isFileOrNull(join("bin", "pypy-c"), join("bin", "pypy"));
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
            // Define PYTHONHOME
            environment.put("PYTHONHOME", getHome().getRemote());
        // Else delete it from environment
        else
            // Delete
            environment.put("PYTHONHOME", null);
        // Check if on Windows
        if (isWindows())
            // If on Windows add home folder and bin folder in PATH
            environment.put("PATH+", getHome().getRemote() + ";" + join("bin").getRemote());
        // Handle UNIX
        else
            // Add bin folder in PATH
            environment.put("PATH+", join("bin").getRemote());
        // Return environment
        return environment;
    }
}
