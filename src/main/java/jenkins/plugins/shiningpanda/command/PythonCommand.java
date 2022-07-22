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

import hudson.FilePath;
import hudson.util.ArgumentListBuilder;
import jenkins.plugins.shiningpanda.utils.StringUtil;

public class PythonCommand extends Command {
    private boolean isUnix;
    private String executable;

    protected PythonCommand(boolean isUnix, String executable, String command, boolean ignoreExitCode) {
        // Call super
        super(command, ignoreExitCode);
        // Store UNIX flag
        this.isUnix = isUnix;
        // Store executable
        this.executable = executable;
    }

    @Override
    protected String getExtension() {
        return ".py";
    }

    @Override
    protected String getContents() {
        return StringUtil.fixCrLf(getCommand());
    }

    @Override
    protected ArgumentListBuilder getArguments(FilePath script) {
        // Get the arguments
        ArgumentListBuilder args = new ArgumentListBuilder(executable, script.getRemote());
        // Check if on UNIX to return the right command
        return isUnix ? args : args.toWindowsCommand();
    }
}
