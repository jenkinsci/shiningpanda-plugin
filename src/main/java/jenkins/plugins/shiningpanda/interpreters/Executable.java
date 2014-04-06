/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2014 ShiningPanda S.A.S.
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
import java.util.HashMap;
import java.util.Map;

import jenkins.plugins.shiningpanda.utils.FilePathUtil;

public class Executable extends Python
{

    /**
     * Constructor using fields.
     * 
     * @param home
     *            The executable
     * @throws InterruptedException
     * @throws IOException
     */
    protected Executable(FilePath home) throws IOException, InterruptedException
    {
        super(home);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.interpreters.Python#isExecutable()
     */
    @Override
    public Executable isExecutable()
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
        // Check if the executable path exists
        return FilePathUtil.isFileOrNull(getHome());
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
        // Get the path value
        String value = getHome().getParent().getRemote();
        // Check if on Windows
        if (isWindows())
            // Add the script folder on Windows
            environment.put("PATH+", value + ";" + getHome().getParent().child("Scripts").getRemote());
        // Return the environment
        return environment;
    }
}
