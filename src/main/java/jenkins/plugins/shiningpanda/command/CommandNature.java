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
package jenkins.plugins.shiningpanda.command;

import hudson.Util;

import java.util.Arrays;
import java.util.List;

import jenkins.plugins.shiningpanda.Messages;

public class CommandNature
{

    /**
     * Shell command
     */
    public final static CommandNature SHELL = new CommandNature("shell", Messages.CommandNature_Shell_DisplayName());

    /**
     * XShell command
     */
    public final static CommandNature XSHELL = new CommandNature("xshell", Messages.CommandNature_XShell_DisplayName());

    /**
     * PYTHON command
     */
    public final static CommandNature PYTHON = new CommandNature("python", Messages.CommandNature_Python_DisplayName());

    /**
     * All natures
     */
    public final static List<CommandNature> ALL = Arrays.asList(SHELL, XSHELL, PYTHON);

    /**
     * The key
     */
    private String key;

    /**
     * The name
     */
    private String name;

    /**
     * Constructor using fields.
     * 
     * @param key
     *            The key
     */
    private CommandNature(String key, String name)
    {
        // Call super
        super();
        // Store the key
        this.key = key;
        // Store the name
        this.name = name;
    }

    /**
     * Get the nature key.
     * 
     * @return The key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Get the nature name.
     * 
     * @return The name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the nature object.
     * 
     * @param raw
     *            The nature key
     * @return The nature object
     */
    public static CommandNature get(String raw)
    {
        // Get the formated value
        String nature = Util.fixEmptyAndTrim(raw);
        // Check if PYTHON nature
        if (PYTHON.getKey().equalsIgnoreCase(nature))
            // Return the PYTHON nature
            return PYTHON;
        // Check if this is the XShell nature
        if (XSHELL.getKey().equalsIgnoreCase(nature))
            // Return the XShell nature
            return XSHELL;
        // By default return the shell nature
        return SHELL;
    }

}
