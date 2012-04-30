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
package jenkins.plugins.shiningpanda.matrix;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

public class PythonAxisTree
{

    /**
     * Pattern to find interpreter and version
     */
    private Pattern pattern;

    /**
     * Default constructor
     */
    public PythonAxisTree()
    {
        // Delegate
        this("^(\\w+)-([\\d\\.]+)$");
    }

    /**
     * Constructor using fields
     * 
     * @param regex
     *            The REGEX
     */
    public PythonAxisTree(String regex)
    {
        // Call super
        super();
        // Compile the pattern
        this.pattern = Pattern.compile(regex);
    }

    /**
     * Get the list of interpreters for the PYTHON installations
     * 
     * @param installations
     *            The PYTHON installations
     * @return The list of interpreters
     */
    public Set<String> getInterpreters(PythonInstallation[] installations)
    {
        // Store interpreters
        Set<String> interpreters = new HashSet<String>();
        // Go threw the installations
        for (PythonInstallation installation : installations)
        {
            // Add the interpreter's name
            interpreters.add(getInterpreter(installation));
        }
        // Return the interpreters
        return interpreters;
    }

    private Matcher getMatcher(PythonInstallation installation)
    {
        return pattern.matcher(installation.getName());
    }

    /**
     * Get the PYTHON interpreter name
     * 
     * @param installation
     *            The PYTHON installation
     * @return The interpreter
     */
    public String getInterpreter(PythonInstallation installation)
    {
        // Get a matcher
        Matcher matcher = getMatcher(installation);
        // Check if found something
        if (matcher.matches())
            // If found something, return the interpreter
            return matcher.group(1);
        // Else return the default interpreter value
        return Messages.PythonAxisTree_Others();
    }

    /**
     * Get the PYTHON interpreter version
     * 
     * @param installation
     *            The PYTHON installation
     * @return The interpreter
     */
    public String getVersion(PythonInstallation installation)
    {
        // Get a matcher
        Matcher matcher = getMatcher(installation);
        // Check if found something
        if (matcher.matches())
            // If found something, return the version
            return matcher.group(2);
        // If version not found, return the installation's name
        return installation.getName();
    }
}
