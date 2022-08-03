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
package jenkins.plugins.shiningpanda.command;

import hudson.Util;
import jenkins.plugins.shiningpanda.Messages;

import java.util.Arrays;
import java.util.List;

public class CommandNature {
    public final static CommandNature SHELL = new CommandNature("shell", Messages.CommandNature_Shell_DisplayName());
    public final static CommandNature XSHELL = new CommandNature("xshell", Messages.CommandNature_XShell_DisplayName());
    public final static CommandNature PYTHON = new CommandNature("python", Messages.CommandNature_Python_DisplayName());
    public final static List<CommandNature> ALL = Arrays.asList(SHELL, XSHELL, PYTHON);
    private String key;
    private String name;

    private CommandNature(String key, String name) {
        // Call super
        super();
        // Store the key
        this.key = key;
        // Store the name
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static CommandNature get(String raw) {
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
