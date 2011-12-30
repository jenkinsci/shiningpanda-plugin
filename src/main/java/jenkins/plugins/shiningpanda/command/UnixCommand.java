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
import hudson.model.Hudson;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Shell;
import jenkins.plugins.shiningpanda.util.StringUtil;

public class UnixCommand extends Command
{

    /**
     * Constructor using fields.
     * 
     * @param command
     *            The content of the execution script
     * @param ignoreExitCode
     *            Is exit code ignored?
     */
    protected UnixCommand(String command, boolean ignoreExitCode)
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
        return ".sh";
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.command.Command#getContents()
     */
    @Override
    protected String getContents()
    {
        return addCrForNonASCII(StringUtil.fixCrLf(getCommand())) + "\n" + "exit 0";
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
        return new String[] { getShell(script.getChannel()), isExitCodeIgnored() ? "-x" : "-xe", script.getRemote() };
    }

    /**
     * Older versions of bash have a bug where non-ASCII on the first line makes
     * the shell think the file is a binary file and not a script. Adding a
     * leading line feed works around this problem.
     * 
     * @param s
     *            The string to fix
     * @return The fixed string
     */
    protected static String addCrForNonASCII(String s)
    {
        if (s.indexOf('\n') != 0)
            return "\n" + s;
        return s;
    }

    /**
     * Get a shell
     * 
     * @param channel
     *            The channel
     * @return The shell to use to launch the script file
     */
    protected static String getShell(VirtualChannel channel)
    {
        return Hudson.getInstance().getDescriptorByType(Shell.DescriptorImpl.class).getShellOrDefault(channel);
    }
}
