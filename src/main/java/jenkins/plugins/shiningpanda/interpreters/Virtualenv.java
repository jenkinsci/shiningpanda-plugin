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
package jenkins.plugins.shiningpanda.interpreters;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.tasks.Messages;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;

import jenkins.plugins.shiningpanda.ShiningPanda;
import jenkins.plugins.shiningpanda.util.FilePathUtil;
import jenkins.plugins.shiningpanda.workspace.Workspace;

public class Virtualenv extends Python
{

    /**
     * Constructor using fields
     * 
     * @param home
     *            The home folder
     */
    public Virtualenv(FilePath home)
    {
        super(home);
    }

    public Virtualenv(Launcher launcher, String home, EnvVars environment)
    {
        super(new FilePath(launcher.getChannel(), environment.expand(home)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.interpreters.Python#isValid()
     */
    @Override
    public boolean isValid() throws IOException, InterruptedException
    {
        // Check if on Windows
        if (isWindows())
        {
            // Look for activate.bat script
            if (FilePathUtil.existsOrNull(join("bin", "activate.bat"), join("Scripts", "activate.bat")) == null)
                // Not found, this VIRTUALENV is not valid
                return false;
        }
        // If on UNIX, look for activate script
        else if (FilePathUtil.existsOrNull(join("bin", "activate")) == null)
            // Not found, this VIRTUALENV is not valid
            return false;
        // Activation script found, look for executable
        return super.isValid();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.interpreters.Python#getEnvironment(boolean)
     */
    @Override
    public EnvVars getEnvironment(boolean withHomeVar) throws IOException, InterruptedException
    {
        EnvVars envVars = new EnvVars();
        envVars.put("PYTHONHOME", null);
        envVars.put("JYTHON_HOME", null);
        envVars.put("VIRTUAL_ENV", getHome().getRemote());
        if (isWindows())
        {
            if (join("bin", "activate.bat").exists())
                envVars.put("PATH+", join("bin").getRemote());
            else
                envVars.put("PATH+", join("Scripts").getRemote());
        }
        else
            envVars.put("PATH+", join("bin").getRemote());
        return envVars;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.interpreters.Python#getExecutable()
     */
    @Override
    public FilePath getExecutable() throws IOException, InterruptedException
    {
        if (isWindows())
            return FilePathUtil.existsOrNull(join("bin", "jython.bat"), join("Scripts", "python.exe"));
        return FilePathUtil.existsOrNull(join("bin", "jython"), join("bin", "python"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.interpreters.Python#isVirtualenv()
     */
    @Override
    public Virtualenv isVirtualenv()
    {
        return this;
    }

    /**
     * Get the file whose last modification date is the creation date of this
     * VIRTUALENV.
     * 
     * @return The time stamp file
     */
    public FilePath getTimestamp()
    {
        return new FilePath(getHome(), ".timestamp");
    }

    /**
     * Check if this VIRTUALENV is out dated. It can be out dated if it does not
     * exist, if it is not valid, or if it was created before the provided time
     * stamp.
     * 
     * @param timestamp
     *            The time stamp
     * @return true if this VIRTUALENV is out dated, else false
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean isOutdated(long timestamp) throws IOException, InterruptedException
    {
        return !isValid() || !getTimestamp().exists() || getTimestamp().lastModified() < timestamp;
    }

    /**
     * Delete this VIRTUALENV
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public void delete() throws IOException, InterruptedException
    {
        getHome().deleteRecursive();
    }

    public boolean create(Launcher launcher, TaskListener listener, Workspace workspace, Python interpreter,
            boolean useDistribute, boolean noSitePackages) throws InterruptedException, IOException
    {
        delete();
        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(interpreter.getExecutable().getRemote());
        args.add(workspace.getVirtualenvPy().getRemote());
        if (useDistribute)
            args.add("--distribute");
        // Add no site packages option
        if (noSitePackages && !ShiningPanda.HOSTED)
            args.add("--no-site-packages");
        FilePath extraSearchDir = workspace.getPackagesDir();
        if (extraSearchDir != null)
            args.addKeyValuePair("--", "extra-search-dir", extraSearchDir.getRemote(), false);
        int r;
        try
        {
            r = launcher.launch().cmds(workspace.isUnix() ? args : args.toWindowsCommand()).envs(interpreter.getEnvironment())
                    .stdout(listener).pwd(workspace.getHome()).join();
        }
        catch (IOException e)
        {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_CommandFailed()));
            r = -1;
        }
        boolean success = r == 0;
        if (success)
            getTimestamp().touch(System.currentTimeMillis());
        return success;
    }
}
