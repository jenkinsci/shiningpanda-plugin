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

import hudson.EnvVars;
import hudson.FilePath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jenkins.plugins.shiningpanda.util.FilePathUtil;

public class Jython extends Python
{

    /**
     * Constructor using fields
     * 
     * @param home
     *            The home folder
     */
    protected Jython(FilePath home)
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
        // On windows this is jython.bat
        if (isWindows())
            return FilePathUtil.isFileOrNull(join("bin", "jython.bat"));
        // On UNIX no extension
        return FilePathUtil.isFileOrNull(join("bin", "jython"));
    }

    @Override
    public Map<String, String> getEnvironment(boolean withHomeVar) throws IOException, InterruptedException
    {
        Map<String, String> environment = new HashMap<String, String>();
        environment.put("JYTHON_HOME", getHome().getRemote());
        environment.put("PATH+", join("bin").getRemote());
        return environment;
    }

}
