/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2012 ShiningPanda S.A.S.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of its license which incorporates the terms and 
 * conditions of version 3 of the GNU Affero General Public License, 
 * supplemented by the additional permissions under the GNU Affero GPL
 * version 3 section 7: if you modify this program, or any covered work, 
 * by linking or combining it with other code, such other code is not 
 * for that reason alone subject to any of the requirements of the GNU
 * Affero GPL version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * license for more details.
 *
 * You should have received a copy of the license along with this program.
 * If not, see <https://raw.github.com/jenkinsci/shiningpanda-plugin/master/LICENSE.txt>.
 */
package jenkins.plugins.shiningpanda.interpreters;

import hudson.FilePath;

import java.io.IOException;
import java.util.Map;

import jenkins.plugins.shiningpanda.utils.FilePathUtil;

public abstract class Python
{

    /**
     * Home folder.
     */
    private FilePath home;

    /**
     * Constructor using fields
     * 
     * @param home
     *            The home folder
     * @throws InterruptedException
     * @throws IOException
     */
    protected Python(FilePath home) throws IOException, InterruptedException
    {
        // Call super
        super();
        // Store home folder with its absolute form
        setHome(home.absolutize());
    }

    /**
     * Get the home folder.
     * 
     * @return The home folder
     */
    public FilePath getHome()
    {
        return home;
    }

    /**
     * Set the home folder.
     * 
     * @param home
     *            The home folder
     */
    private void setHome(FilePath home)
    {
        this.home = home;
    }

    /**
     * Check if this is a valid interpreter.
     * 
     * @return true if this is a valid interpreter
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean isValid() throws IOException, InterruptedException
    {
        // Check that executable exists
        return getExecutable() != null;
    }

    /**
     * Is this PYTHON on Windows?
     * 
     * @return true if on Windows, else false
     * @throws IOException
     * @throws InterruptedException
     */
    protected boolean isWindows() throws IOException, InterruptedException
    {
        return FilePathUtil.isWindows(getHome());
    }

    /**
     * Is this PYTHON on UNIX?
     * 
     * @return true if on UNIX, else false
     * @throws IOException
     * @throws InterruptedException
     */
    protected boolean isUnix() throws IOException, InterruptedException
    {
        return FilePathUtil.isUnix(getHome());
    }

    /**
     * Get the PYTHON executable.
     * 
     * @return The executable file if exists, else null
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract FilePath getExecutable() throws IOException, InterruptedException;

    /**
     * Get the environment for this interpreter.
     * 
     * @param includeHomeKey
     *            If true, add home variable such as PYTHONHOME
     * @return The environment
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract Map<String, String> getEnvironment(boolean includeHomeKey) throws IOException, InterruptedException;

    /**
     * Get the environment for this interpreter with the home variable defined.
     * 
     * @return The environment
     * @throws IOException
     * @throws InterruptedException
     */
    public Map<String, String> getEnvironment() throws IOException, InterruptedException
    {
        return getEnvironment(true);
    }

    /**
     * Create a PYTHON interpreter from its home folder.
     * 
     * @param home
     *            The home folder
     * @return The interpreter if exists, else null
     * @throws IOException
     * @throws InterruptedException
     */
    public static Python fromHome(FilePath home) throws IOException, InterruptedException
    {
        // Get the possible interpreters
        Python[] interpreters = new Python[] { new Executable(home), new Virtualenv(home), new Jython(home), new PyPy(home),
                new IronPython(home), new CPython(home) };
        // Go threw interpreters and try to find a valid one
        for (Python interpreter : interpreters)
            // Check its validity
            if (interpreter.isValid())
                // Found one, return it
                return interpreter;
        // Not found, return null
        return null;
    }

}
