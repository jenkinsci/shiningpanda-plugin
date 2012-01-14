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
