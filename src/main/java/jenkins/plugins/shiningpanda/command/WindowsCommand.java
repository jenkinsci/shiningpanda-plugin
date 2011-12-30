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

public class WindowsCommand extends Command
{

    /**
     * Constructor using fields.
     * 
     * @param command
     *            The content of the execution script
     * @param ignoreExitCode
     *            Is exit code ignored?
     */
    protected WindowsCommand(String command, boolean ignoreExitCode)
    {
        super(command, ignoreExitCode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.command.Command#getExtension()
     */
    @Override
    protected String getExtension()
    {
        return ".bat";
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.command.Command#getContents()
     */
    @Override
    protected String getContents()
    {
        return getCommand() + (isExitCodeIgnored() ? "" : "\r\nexit %ERRORLEVEL%");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.command.Command#getCommandLine(hudson.FilePath
     * )
     */
    @Override
    protected String[] getCommandLine(FilePath script)
    {
        return new String[] { "cmd.exe", "/c", "call", script.getRemote() };
    }

}
