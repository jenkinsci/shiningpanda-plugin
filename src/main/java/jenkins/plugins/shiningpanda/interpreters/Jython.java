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
import java.util.HashMap;
import java.util.Map;

public class Jython extends Python {
    protected Jython(FilePath home) throws IOException, InterruptedException {
        super(home);
    }

    @Override
    public Jython isJython() {
        return this;
    }

    @Override
    public FilePath getExecutable() throws IOException, InterruptedException {
        // For JYTHON 2.2.1, binary is only in the home folder, for later
        // versions use the one in the bin folder (for those versions, do not
        // use the binary available in the home to avoid $JAVA_HOME and
        // $JYTHON_HOME_FALLBACK exports in script header)
        return FilePathUtil.existsOrNull(getExecutable("jython"));
    }

    @Override
    public Map<String, String> getEnvironment(boolean includeHomeKey) throws IOException, InterruptedException {
        // Store the environment
        Map<String, String> environment = new HashMap<>();
        // Check if home variable is required
        if (includeHomeKey)
            // If required define JYTHON_HOME
            environment.put("JYTHON_HOME", getHome().getRemote());
            // Else delete it from environment
        else
            // Delete
            environment.put("JYTHON_HOME", null);
        // Add the bin folder in the PATH
        environment.put("PATH+", getHome().child("bin").getRemote());
        // Return the environment
        return environment;
    }

}
