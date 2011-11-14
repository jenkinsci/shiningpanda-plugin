/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011 ShiningPanda S.A.S.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jenkins.plugins.shiningpanda.command;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.tasks.Messages;

import java.io.IOException;

public abstract class Command
{

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
    protected Command(String command, boolean ignoreExitCode)
    {
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
    protected String getCommand()
    {
        return command;
    }

    /**
     * Set the content of the script to execute.
     * 
     * @param command
     *            The content of the script to execute
     */
    private void setCommand(String command)
    {
        this.command = command;
    }

    /**
     * Is the exit code ignored?
     * 
     * @return true of exit code is ignored, else false
     */
    protected boolean isExitCodeIgnored()
    {
        return ignoreExitCode;
    }

    /**
     * Set the exit code flag.
     * 
     * @param ignoreExitCode
     *            true if the exit code must be ignored
     */
    private void setIgnoreExitCode(boolean ignoreExitCode)
    {
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
    protected abstract String[] getCommandLine(FilePath script);

    /**
     * Create content of the script execute.
     * 
     * @param dir
     *            The folder in which the script is created
     * @return The script file
     * @throws IOException
     * @throws InterruptedException
     */
    protected FilePath createScriptFile(FilePath dir) throws IOException, InterruptedException
    {
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
            throws InterruptedException
    {
        // The script file
        FilePath script = null;
        // Be able to delete the script file
        try
        {
            // Try to create the script file
            try
            {
                // Create the script file
                script = createScriptFile(pwd);
            }
            catch (IOException e)
            {
                // Failed to create the script file, log and return an error
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToProduceScript()));
                return false;
            }
            // Store the exit code
            int r;
            // Be able to get execution errors
            try
            {
                // Execute the script
                r = launcher.launch().cmds(getCommandLine(script)).envs(environment).stdout(listener).pwd(pwd).join();
            }
            catch (IOException e)
            {
                // Failed to execute the script, log and set the flag to return
                // an error
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_CommandFailed()));
                r = -1;
            }
            // Check if successful
            return r == 0;
        }
        finally
        {
            try
            {
                // If script file was created, delete it
                if (script != null)
                    script.delete();
            }
            catch (IOException e)
            {
                // Failed to delete the script, log error only
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToDelete(script)));
            }
        }
    }

    /**
     * Get the right command executor.
     * 
     * @param isUnix
     *            Target execution platform
     * @param command
     *            The content of the script to execute
     * @param ignoreExitCode
     *            Is exit code ignored?
     * @return
     */
    public static Command get(boolean isUnix, String command, boolean ignoreExitCode)
    {
        return (isUnix ? new UnixCommand(command, ignoreExitCode) : new WindowsCommand(command, ignoreExitCode));
    }
}
