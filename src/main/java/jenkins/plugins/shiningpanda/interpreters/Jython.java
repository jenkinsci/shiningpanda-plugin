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

import jenkins.plugins.shiningpanda.utils.FilePathUtil;

public class Jython extends Python
{

    /**
     * Constructor using fields
     * 
     * @param home
     *            The home folder
     * @throws InterruptedException
     * @throws IOException
     */
    protected Jython(FilePath home) throws IOException, InterruptedException
    {
        super(home);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.interpreters.Python#isJython()
     */
    @Override
    public Jython isJython()
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
        // For JYTHON 2.2.1, binary is only in the home folder, for later
        // versions use the one in the bin folder (for those versions, do not
        // use the binary available in the home to avoid $JAVA_HOME and
        // $JYTHON_HOME_FALLBACK exports in script header)
        // Check if on Windows
        if (isWindows())
            // If on Windows, look for .bat
            return FilePathUtil.isFileOrNull(getHome().child("bin").child("jython.bat"), getHome().child("jython.bat"));
        // On UNIX no extension
        return FilePathUtil.isFileOrNull(getHome().child("bin").child("jython"), getHome().child("jython"));
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
        // Check if home variable is required
        if (includeHomeKey)
            // If required define JYTHON_HOME
            environment.put("JYTHON_HOME", getHome().getRemote());
        // Else delete it from environment
        else
            // Delete
            environment.put("JYTHON_HOME", null);
        // Add the bin folder in the PATH
        environment.put("PATH+", getHome().child("bin").getRemote());
        // Return the environment
        return environment;
    }

}
