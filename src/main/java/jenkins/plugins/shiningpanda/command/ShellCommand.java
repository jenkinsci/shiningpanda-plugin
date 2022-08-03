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

import hudson.EnvVars;
import hudson.FilePath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ShellCommand extends Command {
    protected boolean convert;

    protected ShellCommand(String command, boolean ignoreExitCode, boolean convert) {
        // Call super
        super(command, ignoreExitCode);
        // Store conversion flag
        this.convert = convert;
    }

    protected abstract String getSourceContent();

    protected abstract String getSourceSeparator();

    protected abstract String getTargetSeparator();

    protected abstract Pattern getSourceVariable();

    protected abstract String getTargetVariable();

    @Override
    protected EnvVars getEnvironment(FilePath pwd, EnvVars environment) {
        // Check if conversion required
        if (!convert)
            // If not required return the environment directly
            return environment;
        // Get a new one
        environment = new EnvVars(environment);
        // Add the working directory in the path so `./` are useless on UNIX
        environment.override("PATH+", pwd.getRemote());
        // Return the environment
        return environment;
    }

    @Override
    protected final String getContents() {
        // Get the script content
        String contents = getSourceContent();
        // Check if conversion required
        if (!convert)
            // If not return the content directly
            return contents;
        // Get a pattern for the path separator to replace
        String pattern = Pattern.quote(getSourceSeparator());
        // Get the matcher to replace the separator that does not match the
        // platform
        String matcher = Matcher.quoteReplacement(getTargetSeparator());
        // Perform the translation
        contents = contents.replaceAll(pattern, matcher);
        // Get a string buffer to set processed content
        StringBuffer sb = new StringBuffer();
        // Get a variable matcher
        Matcher m = getSourceVariable().matcher(contents);
        // Find variables
        while (m.find())
            // Replace them by the correct ones
            m.appendReplacement(sb, getTargetVariable());
        // Add the end of the content
        m.appendTail(sb);
        // Return the contents
        return sb.toString();
    }

}
