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
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jenkins.plugins.shiningpanda.ShiningPanda;
import jenkins.plugins.shiningpanda.util.EnvVarsUtil;
import jenkins.plugins.shiningpanda.util.FilePathUtil;
import jenkins.plugins.shiningpanda.util.LauncherUtil;
import jenkins.plugins.shiningpanda.workspace.Workspace;

public class Virtualenv extends Python
{

    /**
     * Constructor using fields
     * 
     * @param home
     *            The home folder
     * @throws InterruptedException
     * @throws IOException
     */
    public Virtualenv(FilePath home) throws IOException, InterruptedException
    {
        super(home);
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
    public Map<String, String> getEnvironment(boolean includeHomeKey) throws IOException, InterruptedException
    {
        // Store the environment
        Map<String, String> environment = new HashMap<String, String>();
        // Delete PYTHONHOME variable
        environment.put("PYTHONHOME", null);
        // Delete JYTHON_HOME variable
        environment.put("JYTHON_HOME", null);
        // Check if home variable required
        if (includeHomeKey)
            // Add VIRTUALENV home variable
            environment.put("VIRTUAL_ENV", getHome().getRemote());
        // Else delete it from environment
        else
            // Delete
            environment.put("VIRTUAL_ENV", null);
        // Check if on Windows
        if (isWindows())
        {
            // Check if activation script is in a bin folder or in a scripts one
            if (join("bin", "activate.bat").exists())
                // In bin folder, add this folder to the PATH
                environment.put("PATH+", join("bin").getRemote());
            // In a scripts one
            else
                // Add the scripts folder to the PATH
                environment.put("PATH+", join("Scripts").getRemote());
            // Return the environment
            return environment;
        }
        // For UNIX add the bin folder in the PATH
        environment.put("PATH+", join("bin").getRemote());
        // Return the environment
        return environment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.interpreters.Python#getExecutable()
     */
    @Override
    public FilePath getExecutable() throws IOException, InterruptedException
    {
        // Check if on Windows
        if (isWindows())
            // Look for executables in bin folder if JYTHON, in scripts if
            // standard interpreter
            return FilePathUtil.existsOrNull(join("bin", "jython.bat"), join("Scripts", "python.exe"));
        // On UNIX look for executable in bin folder
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
        return !isValid() || !getTimestamp().exists() || getTimestamp().lastModified() <= timestamp;
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

    /**
     * Create this VIRTUALENV
     * 
     * @param launcher
     *            The launcher
     * @param listener
     *            The listener
     * @param environment
     *            The environment
     * @param workspace
     *            The workspace
     * @param interpreter
     *            The interpreter
     * @param useDistribute
     *            Use DISTRIBUTE or SETUPTOOLS?
     * @param systemSitePackages
     *            Give access to the global site-packages directory
     * @return true if creation was successful, else false
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean create(Launcher launcher, TaskListener listener, EnvVars environment, Workspace workspace,
            Python interpreter, boolean useDistribute, boolean systemSitePackages) throws InterruptedException, IOException
    {
        // Cleanup
        delete();
        // Get the arguments for the command line
        ArgumentListBuilder args = new ArgumentListBuilder();
        // Call PYTHON executable
        args.add(interpreter.getExecutable().getRemote());
        // Path to the script on local computer
        args.add(workspace.getVirtualenvPy().getRemote());
        // If use distribute, add the flag
        if (useDistribute)
            args.add("--distribute");
        // If no site package required, add the flag. If hosted by ShiningPanda
        // always add the flag
        if (systemSitePackages && !ShiningPanda.HOSTED)
            args.add("--system-site-packages");
        // Get the folder where packages can be found (PIP, ...)
        FilePath extraSearchDir = workspace.getPackagesDir();
        // If this folder exists, add as search directory
        if (extraSearchDir != null)
            args.add("--extra-search-dir=" + extraSearchDir.getRemote());
        // Add the place where to create the environment
        args.add(getHome().getRemote());
        // Do not set JYTHON_HOME in environment if this is JYTHON
        // See https://github.com/pypa/virtualenv/issues/185
        boolean includeHomeKey = interpreter.isJython() == null;
        // Start creation
        boolean success = LauncherUtil.launch(launcher, listener, workspace,
                EnvVarsUtil.override(environment, interpreter.getEnvironment(includeHomeKey)), args);
        // Check if was successful
        if (success)
            // If successful, set a creation time stamp
            getTimestamp().touch(System.currentTimeMillis());
        // Return success flag
        return success;
    }

    /**
     * Install a package with PIP.
     * 
     * @param launcher
     *            The launcher
     * @param listener
     *            The listener
     * @param environment
     *            The environment
     * @param workspace
     *            The workspace
     * @param packageName
     *            The package to install
     * @return true if installation was successful, else false
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean pipInstall(Launcher launcher, TaskListener listener, EnvVars environment, Workspace workspace,
            String packageName) throws InterruptedException, IOException
    {
        // Create the arguments for the command line
        ArgumentListBuilder args = new ArgumentListBuilder();
        // Add path to PYTHON executable
        args.add(getExecutable().getRemote());
        // Call PIP via command line
        args.add("-c");
        // Command line script to call PIP
        args.add("import pip; pip.main();");
        // Require an installation
        args.add("install");
        // Get the folder where packages can be found (PIP, ...)
        FilePath extraSearchDir = workspace.getPackagesDir();
        // If this folder exists, add as find link
        if (extraSearchDir != null)
            args.add("-f").add(workspace.getPackagesDir().toURI().toURL().toExternalForm());
        // Ask for upgrade
        args.add("--upgrade");
        // The package to install
        args.add(packageName);
        // Start the process and return status
        return LauncherUtil.launch(launcher, listener, workspace, EnvVarsUtil.override(environment, getEnvironment()), args);
    }

    /**
     * Call TOX.
     * 
     * @param launcher
     *            The launcher
     * @param listener
     *            The listener
     * @param environment
     *            The environment
     * @param workspace
     *            The workspace
     * @param toxIni
     *            The tox.ini file
     * @param recreate
     *            If true recreate the environments
     * @return true if TOX was successful, else false
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean tox(Launcher launcher, TaskListener listener, EnvVars environment, Workspace workspace, String toxIni,
            boolean recreate) throws InterruptedException, IOException
    {
        // Create the arguments for the command line
        ArgumentListBuilder args = new ArgumentListBuilder();
        // Add the path to PYTHON executable
        args.add(getExecutable().getRemote());
        // Call TOX via command line
        args.add("-c");
        // Command line script to call TOX
        args.add("import tox; tox.cmdline();");
        // Add the configuration
        args.add("-c").add(toxIni);
        // Check if recreation asked
        if (recreate)
            args.add("--recreate");
        // Start the process and return status
        return LauncherUtil.launch(launcher, listener, workspace, EnvVarsUtil.override(environment, getEnvironment()), args);
    }
}
