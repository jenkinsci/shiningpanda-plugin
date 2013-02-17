/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2013 ShiningPanda S.A.S.
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
package jenkins.plugins.shiningpanda.tools;

import hudson.FilePath;
import hudson.Platform;
import hudson.tools.ToolProperty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;
import jenkins.plugins.shiningpanda.interpreters.Python;

public class PythonInstallationFinder
{

    /**
     * Configure some installations.
     * 
     * @return The list of configured installations
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<PythonInstallation> configure() throws IOException, InterruptedException
    {
        // Get the installations
        List<PythonInstallation> installations = getInstallations();
        // Set the installations
        Jenkins.getInstance().getDescriptorByType(PythonInstallation.DescriptorImpl.class)
                .setInstallations(installations.toArray(new PythonInstallation[installations.size()]));
        // Return the installations
        return installations;
    }

    /**
     * Get all available PYTHON installations.
     * 
     * @return The available PYTHON installations
     * @throws InterruptedException
     * @throws IOException
     */
    public static List<PythonInstallation> getInstallations() throws IOException, InterruptedException
    {
        // Store installations
        List<PythonInstallation> installations = new ArrayList<PythonInstallation>();
        // Look for candidates
        Map<String, String> candidates = getCandidates();
        // Go threw the candidates
        for (String name : candidates.keySet())
        {
            // Get the potential home folder
            String home = candidates.get(name);
            // Check if an interpreter can be found
            if (Python.fromHome(new FilePath(new File(home))) != null)
                // If found one, add this installation
                installations.add(new PythonInstallation(name, home, Collections.<ToolProperty<?>> emptyList()));
        }
        // Return the installations
        return installations;
    }

    /**
     * Get the possible PYTHON locations.
     * 
     * @return The candidates
     */
    private static LinkedHashMap<String, String> getCandidates()
    {
        // Check if on Windows
        if (Platform.current() == Platform.WINDOWS)
            // If on Windows look for windows candidates
            return getWindowsCandidates();
        // Check if on MacOS
        if (Platform.isDarwin())
            // Get MacOS candidates
            return getDarwinCandidates();
        // Else return Unix candidates
        return getUnixCandidates();
    }

    /**
     * Get the possible PYTHON locations on MacOS.
     * 
     * @return The candidates
     */
    private static LinkedHashMap<String, String> getDarwinCandidates()
    {
        // Store candidates
        LinkedHashMap<String, String> candidates = new LinkedHashMap<String, String>();
        // MacPort
        candidates.put("MacPort-CPython-2.5", "/opt/local/Library/Frameworks/Python.framework/Versions/2.5");
        candidates.put("MacPort-CPython-2.6", "/opt/local/Library/Frameworks/Python.framework/Versions/2.6");
        candidates.put("MacPort-CPython-2.7", "/opt/local/Library/Frameworks/Python.framework/Versions/2.7");
        candidates.put("MacPort-CPython-3.1", "/opt/local/Library/Frameworks/Python.framework/Versions/3.1");
        candidates.put("MacPort-CPython-3.2", "/opt/local/Library/Frameworks/Python.framework/Versions/3.2");
        candidates.put("MacPort-Jython", "/opt/local/share/java/jython");
        candidates.put("MacPort-PyPy", "/opt/local/lib/pypy");
        // System
        candidates.put("System-CPython-2.4", "/System/Library/Frameworks/Python.framework/Versions/2.4");
        candidates.put("System-CPython-2.5", "/System/Library/Frameworks/Python.framework/Versions/2.5");
        candidates.put("System-CPython-2.6", "/System/Library/Frameworks/Python.framework/Versions/2.6");
        candidates.put("System-CPython-2.7", "/System/Library/Frameworks/Python.framework/Versions/2.7");
        candidates.put("System-CPython-3.0", "/System/Library/Frameworks/Python.framework/Versions/3.0");
        candidates.put("System-CPython-3.1", "/System/Library/Frameworks/Python.framework/Versions/3.1");
        candidates.put("System-CPython-3.2", "/System/Library/Frameworks/Python.framework/Versions/3.2");
        // Return candidates
        return candidates;
    }

    /**
     * Get the possible PYTHON locations on Unix.
     * 
     * @return The candidates
     */
    private static LinkedHashMap<String, String> getUnixCandidates()
    {
        // Store candidates
        LinkedHashMap<String, String> candidates = new LinkedHashMap<String, String>();
        // CPython
        candidates.put("System-CPython-2.4", "/usr/bin/python2.4");
        candidates.put("System-CPython-2.5", "/usr/bin/python2.5");
        candidates.put("System-CPython-2.6", "/usr/bin/python2.6");
        candidates.put("System-CPython-2.7", "/usr/bin/python2.7");
        candidates.put("System-CPython-3.0", "/usr/bin/python3.0");
        candidates.put("System-CPython-3.1", "/usr/bin/python3.1");
        candidates.put("System-CPython-3.2", "/usr/bin/python3.2");
        // JYTHON
        candidates.put("System-Jython", "/usr/bin/jython");
        // Return candidates
        return candidates;
    }

    /**
     * Get the possible PYTHON locations on Windows.
     * 
     * @return The candidates
     */
    private static LinkedHashMap<String, String> getWindowsCandidates()
    {
        // Store candidates
        LinkedHashMap<String, String> candidates = new LinkedHashMap<String, String>();
        // CPython
        candidates.put("CPython-2.4", "C:\\Python24");
        candidates.put("CPython-2.5", "C:\\Python25");
        candidates.put("CPython-2.6", "C:\\Python26");
        candidates.put("CPython-2.7", "C:\\Python27");
        candidates.put("CPython-3.0", "C:\\Python30");
        candidates.put("CPython-3.1", "C:\\Python31");
        candidates.put("CPython-3.2", "C:\\Python32");
        // Return candidates
        return candidates;
    }

}