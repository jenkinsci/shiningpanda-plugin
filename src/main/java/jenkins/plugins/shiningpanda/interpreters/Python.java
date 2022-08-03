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
import jenkins.plugins.shiningpanda.utils.FilePathUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Python {
    private FilePath home;

    protected Python(FilePath home) throws IOException, InterruptedException {
        // Call super
        super();
        // Store home folder with its absolute form
        setHome(home.absolutize());
    }

    public FilePath getHome() {
        return home;
    }

    private void setHome(FilePath home) {
        this.home = home;
    }

    public CPython isCPython() {
        return null;
    }

    public PyPy isPyPy() {
        return null;
    }

    public Jython isJython() {
        return null;
    }

    public IronPython isIronPython() {
        return null;
    }

    public Virtualenv isVirtualenv() {
        return null;
    }

    public Executable isExecutable() {
        return null;
    }

    public boolean isValid() throws IOException, InterruptedException {
        // Check that executable exists
        return getExecutable() != null;
    }

    protected boolean isWindows() throws IOException, InterruptedException {
        return FilePathUtil.isWindows(getHome());
    }

    protected boolean isUnix() throws IOException, InterruptedException {
        return FilePathUtil.isUnix(getHome());
    }

    public abstract FilePath getExecutable() throws IOException, InterruptedException;

    public FilePath getExecutable(String executable) throws IOException, InterruptedException {
        List<String> executableSuffixes = new ArrayList<>();
        if (isWindows()) {
            executableSuffixes.add(".exe");
            executableSuffixes.add(".bat");
            executableSuffixes.add(".cmd");
            executableSuffixes.add(".ps1");
        } else {
            executableSuffixes.add("");
        }
        List<String> childDirectories = new ArrayList<>();
        childDirectories.add("bin");
        childDirectories.add("Scripts");

        for (String executableSuffix : executableSuffixes) {
            String fullExecutable = executable + executableSuffix;
            FilePath executablePath;
            for (String childDirectory : childDirectories) {
                FilePath childPath = getHome().child(childDirectory);
                if (childPath.exists()) {
                    executablePath = childPath.child(executable + executableSuffix);
                    if (executablePath.exists()) {
                        return executablePath;
                    }
                }
            }
            executablePath = getHome().child(fullExecutable);
            if (executablePath.exists()) {
                return executablePath;
            }
        }
        return null;
    }

    public abstract Map<String, String> getEnvironment(boolean includeHomeKey) throws
            IOException, InterruptedException;

    public Map<String, String> getEnvironment() throws IOException, InterruptedException {
        return getEnvironment(true);
    }

    public static Python fromHome(FilePath home) throws IOException, InterruptedException {
        // Get the possible interpreters
        Python[] interpreters = new Python[]{new Executable(home), new Virtualenv(home), new Jython(home),
                new PyPy(home), new IronPython(home), new CPython(home)};
        // Go threw interpreters and try to find a valid one
        for (Python interpreter : interpreters)
            // Check its validity
            if (interpreter.isValid())
                // Found one, return it
                return interpreter;
        // Not found, return null
        return null;
    }

}
