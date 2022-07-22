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

import java.util.regex.Pattern;

public class WindowsCommand extends ShellCommand {
    private final static Pattern VARIABLE = Pattern.compile("\\$\\{?(\\w+)\\}?");

    protected WindowsCommand(String command, boolean ignoreExitCode, boolean convert) {
        super(command, ignoreExitCode, convert);
    }

    @Override
    protected String getExtension() {
        return ".bat";
    }

    @Override
    protected String getSourceContent() {
        return getCommand() + "\r\nexit %ERRORLEVEL%";
    }

    @Override
    protected String getSourceSeparator() {
        return "/";
    }

    @Override
    protected String getTargetSeparator() {
        return "\\";
    }

    @Override
    protected Pattern getSourceVariable() {
        return VARIABLE;
    }

    @Override
    protected String getTargetVariable() {
        return "%$1%";
    }

    @Override
    protected ArgumentListBuilder getArguments(FilePath script) {
        return new ArgumentListBuilder("cmd.exe", "/c", "call", script.getRemote());
    }

}
