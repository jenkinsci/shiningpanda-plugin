/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2012 ShiningPanda S.A.S.
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

import hudson.FilePath;
import hudson.util.ArgumentListBuilder;
import jenkins.plugins.shiningpanda.utils.StringUtil;

public class PythonCommand extends Command
{

    /**
     * Is this on UNIX?
     */
    private boolean isUnix;

    /**
     * Store PYTHON executable
     */
    private String executable;

    /**
     * Constructor using fields.
     * 
     * @param isUnix
     *            Is this on UNIX?
     * @param executable
     *            The PYTHON executable
     * @param command
     *            The content of the execution script
     * @param ignoreExitCode
     *            Is exit code ignored?
     */
    protected PythonCommand(boolean isUnix, String executable, String command, boolean ignoreExitCode)
    {
        // Call super
        super(command, ignoreExitCode);
        // Store UNIX flag
        this.isUnix = isUnix;
        // Store executable
        this.executable = executable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.command.Command#getExtension()
     */
    @Override
    protected String getExtension()
    {
        return ".py";
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.command.Command#getContents()
     */
    @Override
    protected String getContents()
    {
        return StringUtil.fixCrLf(getCommand());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.command.Command#getArguments(hudson.FilePath
     * )
     */
    @Override
    protected ArgumentListBuilder getArguments(FilePath script)
    {
        // Get the arguments
        ArgumentListBuilder args = new ArgumentListBuilder(executable, script.getRemote());
        // Check if on UNIX to return the right command
        return isUnix ? args : args.toWindowsCommand();
    }
}
