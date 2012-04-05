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
package jenkins.plugins.shiningpanda.interpreters;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.plugins.shiningpanda.ShiningPanda;
import jenkins.plugins.shiningpanda.utils.EnvVarsUtil;
import jenkins.plugins.shiningpanda.utils.FilePathUtil;
import jenkins.plugins.shiningpanda.utils.LauncherUtil;
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
            if (FilePathUtil.existsOrNull(getHome().child("bin").child("activate.bat"),
                    getHome().child("Scripts").child("activate.bat")) == null)
                // Not found, this VIRTUALENV is not valid
                return false;
        }
        // If on UNIX, look for activate script
        else if (FilePathUtil.existsOrNull(getHome().child("bin").child("activate")) == null)
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
            if (getHome().child("bin").child("activate.bat").exists())
                // In bin folder, add this folder to the PATH
                environment.put("PATH+", getHome().child("bin").getRemote());
            // In a scripts one
            else
                // Add the scripts folder to the PATH
                environment.put("PATH+", getHome().child("Scripts").getRemote());
            // Return the environment
            return environment;
        }
        // Handle UNIX case
        else
        {
            // Get a potential library folder
            FilePath lib = getHome().child("lib");
            // Check if there is a library folder containing some shared
            // libraries
            if (!FilePathUtil.listSharedLibraries(lib).isEmpty())
                // Export in environment
                environment.putAll(EnvVarsUtil.getLibs(lib));
        }
        // For UNIX add the bin folder in the PATH
        environment.put("PATH+", getHome().child("bin").getRemote());
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
            // Look for executables in bin folder if JYTHON or PYPY, in scripts
            // if standard interpreter
            return FilePathUtil.existsOrNull(getHome().child("bin").child("jython.bat"),
                    getHome().child("bin").child("pypy.exe"), getHome().child("Scripts").child("python.exe"));
        // On UNIX look for executable in bin folder
        return FilePathUtil.existsOrNull(getHome().child("bin").child("jython"), getHome().child("bin").child("pypy"),
                getHome().child("bin").child("python"));
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
     * Check if this VIRTUALENV is out dated. It can be out dated if it does not
     * exist, if it is not valid, or if the signature doesn't match the desired
     * one.
     * 
     * @param workspace
     *            The workspace
     * @param interpreter
     *            The interpreter
     * @param useDistribute
     *            True id use distribute, else false
     * @param systemSitePackages
     *            True if include system packages, else false
     * @return true if this VIRTUALENV is out dated, else false
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean isOutdated(Workspace workspace, Python interpreter, boolean useDistribute, boolean systemSitePackages)
            throws IOException, InterruptedException
    {
        // Out dated if invalid, or if no signature file, or if signatures
        // differ
        return !isValid()
                || !getSignatureFile().exists()
                || !FilePathUtil.read(getSignatureFile(), "UTF-8").equals(
                        getSignature(workspace, interpreter, useDistribute, systemSitePackages));
    }

    /**
     * Get the signature file path.
     * 
     * @return The signature file path
     */
    public FilePath getSignatureFile()
    {
        return getHome().child(".signature");
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
     * @param workspace
     *            The workspace
     * @param pwd
     *            The working directory
     * @param environment
     *            The environment
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
    public boolean create(Launcher launcher, TaskListener listener, Workspace workspace, FilePath pwd, EnvVars environment,
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
            // Add the flag
            args.add("--distribute");
        // If no site package required, add the flag. If hosted by ShiningPanda
        // always add the flag
        if (systemSitePackages && !ShiningPanda.HOSTED)
            // Add the flag
            args.add("--system-site-packages");
        // Get the folder where packages can be found (PIP, ...)
        FilePath extraSearchDir = workspace.getPackagesDir();
        // If this folder exists, add as search directory
        if (extraSearchDir != null)
            // Add search folders
            args.add("--extra-search-dir=" + extraSearchDir.getRemote());
        // Add the place where to create the environment
        args.add(getHome().getRemote());
        // Do not set JYTHON_HOME in environment if this is JYTHON
        // See https://github.com/pypa/virtualenv/issues/185
        boolean includeHomeKey = interpreter.isJython() == null;
        // Start creation
        boolean success = LauncherUtil.launch(launcher, listener, pwd,
                EnvVarsUtil.override(environment, interpreter.getEnvironment(includeHomeKey)), args);
        // Add links to libraries
        // See https://github.com/pypa/virtualenv/issues/216
        if (isUnix())
        {
            // Get the list of libraries
            List<FilePath> libs = FilePathUtil.listSharedLibraries(interpreter);
            // Check if got at least one
            if (!libs.isEmpty())
            {
                // Get the VIRTUALENV library folder
                FilePath libDir = getHome().child("lib");
                // Create it if required
                libDir.mkdirs();
                // Go threw the libraries and create links
                for (FilePath lib : libs)
                {
                    // Get the link path
                    FilePath link = libDir.child(lib.getName());
                    // Check that not already exists
                    if (!link.exists())
                        // Create the link
                        if (!LauncherUtil.createSymlink(launcher, listener, lib, link))
                            // Failed to create link
                            return false;
                }
            }
        }
        // Check if was successful
        if (success)
            // Write down the virtual environment signature
            getSignatureFile().write(getSignature(workspace, interpreter, useDistribute, systemSitePackages), "UTF-8");
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
     * @param workspace
     *            The workspace
     * @param pwd
     *            The working directory
     * @param environment
     *            The environment
     * @param packageName
     *            The package to install
     * @return true if installation was successful, else false
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean pipInstall(Launcher launcher, TaskListener listener, Workspace workspace, FilePath pwd, EnvVars environment,
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
            // Add flag
            args.add("-f").add(workspace.getPackagesDir().toURI().toURL().toExternalForm());
        // Ask for upgrade
        args.add("--upgrade");
        // The package to install
        args.add(packageName);
        // Start the process and return status
        return LauncherUtil.launch(launcher, listener, pwd, EnvVarsUtil.override(environment, getEnvironment()), args);
    }

    /**
     * Call TOX.
     * 
     * @param launcher
     *            The launcher
     * @param listener
     *            The listener
     * @param pwd
     *            The working directory
     * @param environment
     *            The environment
     * @param toxIni
     *            The tox.ini file
     * @param recreate
     *            If true recreate the environments
     * @return true if TOX was successful, else false
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean tox(Launcher launcher, TaskListener listener, FilePath pwd, EnvVars environment, String toxIni,
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
            // Add the flag
            args.add("--recreate");
        // Start the process and return status
        return LauncherUtil.launch(launcher, listener, pwd, EnvVarsUtil.override(environment, getEnvironment()), args);
    }

    /**
     * Bootstrap BUILDOUT and start its binary.
     * 
     * @param launcher
     *            The launcher
     * @param listener
     *            The listener
     * @param workspace
     *            The workspace
     * @param pwd
     *            The working folder
     * @param environment
     *            The environment
     * @param buildoutCfg
     *            The BUILDOUT configuration file
     * @param useDistribute
     *            Use DISTRIBUTE instead of SETUPTOOLS
     * @return true if setup was successful, else false
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean buildout(Launcher launcher, TaskListener listener, Workspace workspace, FilePath pwd, EnvVars environment,
            String buildoutCfg, boolean useDistribute) throws InterruptedException, IOException
    {
        // Get the environment
        EnvVars finalEnvironment = EnvVarsUtil.override(environment, getEnvironment());
        // Create the arguments for the command line
        ArgumentListBuilder args = new ArgumentListBuilder();
        // Add the path to PYTHON executable
        args.add(getExecutable().getRemote());
        // Path to the script on local computer
        args.add(workspace.getBootstrapPy().getRemote());
        // Add the configuration
        args.add("-c").add(buildoutCfg);
        // If use distribute, add the flag
        if (useDistribute)
            // Add the flag
            args.add("--distribute");
        // Start bootstrap
        if (!LauncherUtil.launch(launcher, listener, pwd, finalEnvironment, args))
            // Failed to bootstrap, no need to go further
            return false;
        // Call BUILDOUT and return status
        args = new ArgumentListBuilder();
        args.add(pwd.child(buildoutCfg)
                .getParent().child("bin").child("buildout").getRemote());
        args.add("-c").add(buildoutCfg);
        return LauncherUtil.launch(launcher, listener, pwd, finalEnvironment, args);
    }

    /**
     * Get a virtual environment signature.
     * 
     * @param workspace
     *            The workspace
     * @param interpreter
     *            The interpreter
     * @param useDistribute
     *            Use distribute package
     * @param systemSitePackages
     *            Use system packages
     * @return The signature
     * @throws IOException
     * @throws InterruptedException
     */
    public static String getSignature(Workspace workspace, Python interpreter, boolean useDistribute, boolean systemSitePackages)
            throws IOException, InterruptedException
    {
        StringBuilder sb = new StringBuilder();
        // Get the executable
        FilePath executable = interpreter.getExecutable();
        // Add the path to executable
        sb.append(executable.getRemote()).append("\n");
        // Add the executable MD5
        sb.append(executable.digest()).append("\n");
        // Get the VIRTUALENV script digest
        sb.append(workspace.getMasterVirtualenvPy().digest()).append("\n");
        // Add the distribute flag
        sb.append(useDistribute).append("\n");
        // Add the systemSitePackages flag
        sb.append(systemSitePackages).append("\n");
        // Get the folder containing packages on the master
        FilePath packageDir = workspace.getMasterPackagesDir();
        // Check if this folder exists
        if (packageDir != null)
            // If exists, list the packages
            for (FilePath bn : packageDir.list())
                // Add their names
                sb.append(bn.getName()).append("\n");
        // Go threw the shared libraries
        for (FilePath lib : FilePathUtil.listSharedLibraries(interpreter))
            // Add their names
            sb.append(lib.getName()).append("\n");
        // Return the signature
        return sb.toString();
    }
}
