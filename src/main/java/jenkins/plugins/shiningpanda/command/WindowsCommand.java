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

import java.util.regex.Pattern;

public class WindowsCommand extends ShellCommand
{

    /**
     * Store the variable pattern.
     */
    private final static Pattern VARIABLE = Pattern.compile("\\$\\{?(\\w+)\\}?");

    /**
     * Constructor using fields.
     * 
     * @param command
     *            The content of the execution script
     * @param ignoreExitCode
     *            Is exit code ignored?
     * @param convert
     *            Convert shell to batch
     */
    protected WindowsCommand(String command, boolean ignoreExitCode, boolean convert)
    {
        super(command, ignoreExitCode, convert);
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
     * @see
     * jenkins.plugins.shiningpanda.command.ShellCommand#getOriginalContent()
     */
    @Override
    protected String getSourceContent()
    {
        return getCommand() + "\r\nexit %ERRORLEVEL%";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.command.ShellCommand#getSourceSeparator()
     */
    @Override
    protected String getSourceSeparator()
    {
        return "/";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.command.ShellCommand#getTargetSeparator()
     */
    @Override
    protected String getTargetSeparator()
    {
        return "\\";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.command.ShellCommand#getSourceVariable()
     */
    @Override
    protected Pattern getSourceVariable()
    {
        return VARIABLE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.command.ShellCommand#getTargetVariable()
     */
    @Override
    protected String getTargetVariable()
    {
        return "%$1%";
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
        return new ArgumentListBuilder("cmd.exe", "/c", "call", script.getRemote());
    }

}
