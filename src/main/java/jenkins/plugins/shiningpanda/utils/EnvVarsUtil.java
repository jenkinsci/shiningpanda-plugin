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
package jenkins.plugins.shiningpanda.utils;

import hudson.EnvVars;
import hudson.FilePath;

import java.util.HashMap;
import java.util.Map;

public class EnvVarsUtil
{

    /**
     * Get the library environment for the provided folder.
     * 
     * @param filePath
     *            The folder
     * @return The environment
     */
    public static Map<String, String> getLibs(FilePath filePath)
    {
        // Create the map to store the environment
        Map<String, String> environment = new HashMap<String, String>();
        // General UNIX
        environment.put("LD_LIBRARY_PATH+", filePath.getRemote());
        // OSX
        environment.put("DYLD_LIBRARY_PATH+", filePath.getRemote());
        // AIX
        environment.put("LIBPATH+", filePath.getRemote());
        // HP-UX
        environment.put("SHLIB_PATH+", filePath.getRemote());
        // Return the environment
        return environment;
    }

    /**
     * Return the list of variables used to define an installation home.
     * 
     * @return The list of variables
     */
    public static String[] getPythonHomeKeys()
    {
        return new String[] { "PYTHONHOME", "JYTHON_HOME", "VIRTUAL_ENV" };
    }

    /**
     * Clone the original environment and override it with additional one
     * (potentially containing some "+").
     * 
     * @param original
     *            The original environment
     * @param additional
     *            The environment that overrides the original one
     * @return The environment object
     */
    public static EnvVars override(EnvVars original, Map<String, String> additional)
    {
        // Copy the original environment
        EnvVars environment = new EnvVars(original);
        // Override all to suppress all "+"
        environment.overrideAll(additional);
        // Return the object
        return environment;
    }

    /**
     * Expand a value with the current environment variable.
     * 
     * @param value
     *            The value to expand
     * @return The expanded value
     */
    public static String expand(String value)
    {
        return new EnvVars(EnvVars.masterEnvVars).expand(value);
    }

}
