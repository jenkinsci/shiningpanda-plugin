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

import jenkins.plugins.shiningpanda.util.FilePathUtil;

public class CPython extends Python
{

    /**
     * Constructor using fields
     * 
     * @param home
     *            The home folder
     */
    protected CPython(FilePath home)
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
        // If on Windows, look for python.exe
        if (isWindows())
            return FilePathUtil.isFileOrNull(join("python.exe"));
        // If on UNIX, this can be PYTHON 3 or PYTHON 2
        return FilePathUtil.isFileOrNull(join("bin", "python3"), join("bin", "python"));
    }

    @Override
    public EnvVars getEnvironment(boolean withHomeVar) throws IOException, InterruptedException
    {
        EnvVars envVars = new EnvVars();
        envVars.put("PYTHONHOME", getHome().getRemote());
        if (isWindows())
            envVars.put("PATH+", getHome().getRemote() + ";" + join("Scripts").getRemote());
        else
            envVars.put("PATH+", join("bin").getRemote());
        return envVars;
    }

}
