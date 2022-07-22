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
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;

public abstract class Command {
    private String command;
    private boolean ignoreExitCode;

    protected Command(String command, boolean ignoreExitCode) {
        // Call super
        super();
        // Store the content of the script
        setCommand(command);
        // Store the exit code flag
        setIgnoreExitCode(ignoreExitCode);
    }

    protected String getCommand() {
        return command;
    }

    private void setCommand(String command) {
        this.command = command;
    }

    protected boolean isExitCodeIgnored() {
        return ignoreExitCode;
    }

    private void setIgnoreExitCode(boolean ignoreExitCode) {
        this.ignoreExitCode = ignoreExitCode;
    }

    protected abstract String getExtension();

    protected abstract String getContents();

    protected abstract ArgumentListBuilder getArguments(FilePath script);

    protected EnvVars getEnvironment(FilePath pwd, EnvVars environment) {
        return environment;
    }

    protected FilePath createScriptFile(FilePath dir) throws IOException, InterruptedException {
        return dir.createTextTempFile("shiningpanda", getExtension(), getContents(), false);
    }

    public boolean launch(Launcher launcher, TaskListener listener, EnvVars environment, FilePath pwd)
            throws InterruptedException {
        // The script file
        FilePath script = null;
        // Be able to delete the script file in all cases
        try {
            // Try to create the script file
            try {
                // Create the script file
                script = createScriptFile(pwd);
            }
            // Failed to create the script file
            catch (IOException e) {
                // Display exception
                Util.displayIOException(e, listener);
                // Log the message
                e.printStackTrace(listener.fatalError("Unable to produce a script file"));
                // Do not continue
                return false;
            }
            // Be able to catch execution errors
            try {
                // Execute the script
                int exitCode = launcher.launch().cmds(getArguments(script)).envs(getEnvironment(pwd, environment))
                        .stdout(listener).pwd(pwd).join();
                // Check if continue or not depending on the exit code ignore
                // flag
                return isExitCodeIgnored() ? true : exitCode == 0;
            }
            // Failed to execute the script
            catch (IOException e) {
                // Display exception
                Util.displayIOException(e, listener);
                // Log the error
                e.printStackTrace(listener.fatalError("command execution failed"));
                // Do not continue
                return false;
            }
        }
        // Cleanup in all cases
        finally {
            // Catch cleanup errors
            try {
                // Check if the script was created
                if (script != null)
                    // Delete it
                    script.delete();
            } catch (IOException e) {
                // Failed to delete the script, display exception
                Util.displayIOException(e, listener);
                // Log the error
                e.printStackTrace(listener.fatalError("Unable to delete script file {0}", script));
            }
        }
    }

    public static Command get(boolean isUnix, String executable, CommandNature nature, String command,
                              boolean ignoreExitCode) {
        // Check if this is a PYTHON script
        if (nature == CommandNature.PYTHON)
            // Create a new PYTHON command
            return new PythonCommand(isUnix, executable, command, ignoreExitCode);
        // Check if a conversion is required
        boolean convert = nature == CommandNature.XSHELL;
        // Create the right command depending of the OS and the conversion flag
        return (isUnix ? new UnixCommand(command, ignoreExitCode, convert)
                : new WindowsCommand(command, ignoreExitCode, convert));
    }
}
