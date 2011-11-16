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
import java.util.Map;

import jenkins.plugins.shiningpanda.util.FilePathUtil;

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
     * Is this a CPython implementation?
     * 
     * @return true if this is a CPython implementation, else false
     */
    public CPython isCPython()
    {
        return null;
    }

    /**
     * Is this a PyPy implementation?
     * 
     * @return true if this is a PyPy implementation, else false
     */
    public PyPy isPyPy()
    {
        return null;
    }

    /**
     * Is this a JYTHON implementation?
     * 
     * @return true if this is a JYTHON implementation, else false
     */
    public Jython isJython()
    {
        return null;
    }

    /**
     * Is this a VIRTUALENV?
     * 
     * @return true if this is a VIRTUALENV, else false
     */
    public Virtualenv isVirtualenv()
    {
        return null;
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
     * Join path parts to home folder.
     * 
     * @param parts
     *            The path parts
     * @return The resulting file
     * @throws IOException
     * @throws InterruptedException
     */
    protected FilePath join(String... parts) throws IOException, InterruptedException
    {
        return FilePathUtil.join(getHome(), parts);
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
        Python[] interpreters = new Python[] { new Virtualenv(home), new Jython(home), new PyPy(home), new CPython(home) };
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
