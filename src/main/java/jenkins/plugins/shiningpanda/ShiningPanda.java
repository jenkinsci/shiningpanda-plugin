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
package jenkins.plugins.shiningpanda;

import hudson.Main;
import hudson.Plugin;
import jenkins.plugins.shiningpanda.tools.PythonInstallationFinder;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

public class ShiningPanda extends Plugin
{

    /**
     * Is JENKINS hosted on shiningpanda.com?
     */
    public static boolean HOSTED = Boolean.getBoolean(ShiningPanda.class.getName() + ".hosted");

    /*
     * (non-Javadoc)
     * 
     * @see hudson.Plugin#start()
     */
    @Override
    public void start() throws Exception
    {
        // Enable backward compatibility
        Compatibility.enable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see hudson.Plugin#postInitialize()
     */
    @Override
    public void postInitialize() throws Exception
    {
        // Check if some installations are not already set or if this is not in
        // test context
        if (PythonInstallation.isEmpty() && !Main.isUnitTest)
            // Look for installations and configure them
            PythonInstallationFinder.configure();
    }
}
