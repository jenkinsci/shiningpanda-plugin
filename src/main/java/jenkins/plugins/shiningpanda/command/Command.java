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

import java.io.IOException;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.tasks.Messages;
import hudson.util.ArgumentListBuilder;

public abstract class Command {

    /**
     * Content of the execution script.
     */
    private String command;

    /**
     * Is exit code ignored?
     */
    private boolean ignoreExitCode;

    /**
     * Constructor using fields.
     * 
     * @param command
     *            The content of the execution script
     * @param ignoreExitCode
     *            Is exit code ignored?
     */
    protected Command(String command, boolean ignoreExitCode) {
	// Call super
	super();
	// Store the content of the script
	setCommand(command);
	// Store the exit code flag
	setIgnoreExitCode(ignoreExitCode);
    }

    /**
     * Get the content of the script to execute.
     * 
     * @return The content of the script to execute
     */
    protected String getCommand() {
	return command;
    }

    /**
     * Set the content of the script to execute.
     * 
     * @param command
     *            The content of the script to execute
     */
    private void setCommand(String command) {
	this.command = command;
    }

    /**
     * Is the exit code ignored?
     * 
     * @return true of exit code is ignored, else false
     */
    protected boolean isExitCodeIgnored() {
	return ignoreExitCode;
    }

    /**
     * Set the exit code flag.
     * 
     * @param ignoreExitCode
     *            true if the exit code must be ignored
     */
    private void setIgnoreExitCode(boolean ignoreExitCode) {
	this.ignoreExitCode = ignoreExitCode;
    }

    /**
     * Get the extension of the script file.
     * 
     * @return The extension
     */
    protected abstract String getExtension();

    /**
     * Get the content of the script file
     * 
     * @return The content of the file
     */
    protected abstract String getContents();

    /**
     * Get the command line to execute.
     * 
     * @param script
     *            The script to execute
     * @return The arguments
     */
    protected abstract ArgumentListBuilder getArguments(FilePath script);

    /**
     * Be able to have a late environment processing.
     * 
     * @param pwd
     *            The working directory
     * @param environment
     *            The environment
     * @return The processed environment
     */
    protected EnvVars getEnvironment(FilePath pwd, EnvVars environment) {
	return environment;
    }

    /**
     * Create content of the script execute.
     * 
     * @param dir
     *            The folder in which the script is created
     * @return The script file
     * @throws IOException
     * @throws InterruptedException
     */
    protected FilePath createScriptFile(FilePath dir) throws IOException, InterruptedException {
	return dir.createTextTempFile("shiningpanda", getExtension(), getContents(), false);
    }

    /**
     * Launch the command.
     * 
     * @param launcher
     *            The launcher
     * @param listener
     *            The listener
     * @param environment
     *            The environment
     * @param pwd
     *            The current directory
     * @return true if successful, else false
     * @throws InterruptedException
     */
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
		e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToProduceScript()));
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
		e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_CommandFailed()));
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
		e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToDelete(script)));
	    }
	}
    }

    /**
     * Get the right command executor.
     * 
     * @param isUnix
     *            Target execution platform
     * @param executable
     *            The PYTHON executable
     * @param nature
     *            The nature of the command: PYTHON, shell, X shell
     * @param command
     *            The content of the script to execute
     * @param ignoreExitCode
     *            Is exit code ignored?
     * @return The command object
     */
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
