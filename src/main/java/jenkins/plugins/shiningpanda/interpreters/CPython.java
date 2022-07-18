/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2015 ShiningPanda S.A.S.
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
import jenkins.plugins.shiningpanda.utils.EnvVarsUtil;
import jenkins.plugins.shiningpanda.utils.FilePathUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CPython extends Python {

    /**
     * Constructor using fields
     *
     * @param home The home folder
     * @throws InterruptedException
     * @throws IOException
     */
    protected CPython(FilePath home) throws IOException, InterruptedException {
        super(home);
    }

    /*
     * (non-Javadoc)
     *
     * @see jenkins.plugins.shiningpanda.interpreters.Python#isCPython()
     */
    @Override
    public CPython isCPython() {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see jenkins.plugins.shiningpanda.interpreters.Python#getExecutable()
     */
    @Override
    public FilePath getExecutable() throws IOException, InterruptedException {
        // Check if on Windows
        if (isWindows())
            // If on Windows, look for python.exe
            return FilePathUtil.isFileOrNull(getHome().child("python.exe"));
        // If on UNIX, this can be PYTHON 3 or PYTHON 2
        return FilePathUtil.isFileOrNull(getHome().child("bin").child("python3"),
                getHome().child("bin").child("python"));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * jenkins.plugins.shiningpanda.interpreters.Python#getEnvironment(boolean)
     */
    @Override
    public Map<String, String> getEnvironment(boolean includeHomeKey) throws IOException, InterruptedException {
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
        else {
            // Add the bin folder in the PATH
            environment.put("PATH+", getHome().child("bin").getRemote());
            // Add the library folder in the path to look for libraries
            environment.putAll(EnvVarsUtil.getLibs(getHome().child("lib")));
        }
        // Return the environment
        return environment;
    }

}
