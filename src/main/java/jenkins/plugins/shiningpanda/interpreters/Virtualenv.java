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
package jenkins.plugins.shiningpanda.interpreters;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import jenkins.plugins.shiningpanda.utils.EnvVarsUtil;
import jenkins.plugins.shiningpanda.utils.FilePathUtil;
import jenkins.plugins.shiningpanda.utils.LauncherUtil;
import jenkins.plugins.shiningpanda.workspace.Workspace;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Virtualenv extends Python {
    public Virtualenv(FilePath home) throws IOException, InterruptedException {
        super(home);
    }

    @Override
    public Virtualenv isVirtualenv() {
        return this;
    }

    @Override
    public boolean isValid() throws IOException, InterruptedException {
        FilePath activateScript = getExecutable("activate");
        if (activateScript == null || !activateScript.exists()) {
            return false;
        }
        // Activation script found, look for executable
        return super.isValid();
    }

    @Override
    public Map<String, String> getEnvironment(boolean includeHomeKey) throws IOException, InterruptedException {
        // Store the environment
        Map<String, String> environment = new HashMap<>();
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
        if (isWindows()) {
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
        else {
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

    @Override
    public FilePath getExecutable() throws IOException, InterruptedException {
        return FilePathUtil.existsOrNull(getExecutable("jython"),
                getExecutable("pypy"), getExecutable("python"));
    }

    public boolean isOutdated(Workspace workspace, Python interpreter, boolean systemSitePackages)
            throws IOException, InterruptedException {
        // Out dated if invalid, or if no signature file, or if signatures
        // differ
        return !isValid() || !getSignatureFile().exists() || !FilePathUtil.read(getSignatureFile(), "UTF-8")
                .equals(getSignature(workspace, interpreter, systemSitePackages));
    }

    public FilePath getSignatureFile() {
        return getHome().child(".signature");
    }

    public void delete() throws IOException, InterruptedException {
        getHome().deleteRecursive();
    }

    public boolean create(Launcher launcher, TaskListener listener, Workspace workspace, FilePath pwd,
                          EnvVars environment, Python interpreter, boolean systemSitePackages, boolean upgradeDependencies)
            throws InterruptedException, IOException {
        // Cleanup
        delete();
        // Get the arguments for the command line
        ArgumentListBuilder args = new ArgumentListBuilder();
        // Call PYTHON executable
        args.add(interpreter.getExecutable().getRemote());
        // Path to the script on local computer
        args.add("-m");
        args.add("venv");
        // If use system site package, add the flag
        if (systemSitePackages) {
            // Add the flag
            args.add("--system-site-packages");
        }
        if (upgradeDependencies) {
            args.add("--upgrade-deps");
        }
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
        // Check if was successful
        if (success) {
            // Write down the virtual environment signature
            getSignatureFile().write(getSignature(workspace, interpreter, systemSitePackages), "UTF-8");
        }
        // Return success flag
        return success;
    }

    public boolean pipInstall(Launcher launcher, TaskListener listener, Workspace workspace, FilePath pwd,
                              EnvVars environment, String packageName) throws InterruptedException, IOException {
        // Create the arguments for the command line
        ArgumentListBuilder args = new ArgumentListBuilder();
        // Add path to PYTHON executable
        args.add(getExecutable().getRemote());
        // Call PIP via command line
        args.add("-m");
        args.add("pip");
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

    public boolean tox(Launcher launcher, TaskListener listener, FilePath pwd, EnvVars environment, String toxIni,
                       boolean recreate) throws InterruptedException, IOException {
        // Create the arguments for the command line
        ArgumentListBuilder args = new ArgumentListBuilder();
        // Call TOX via command line
        args.add(getExecutable("tox").getRemote());
        // Add the configuration
        args.add("-c").add(toxIni);
        // Check if recreation asked
        if (recreate)
            // Add the flag
            args.add("--recreate");
        // Start the process and return status
        return LauncherUtil.launch(launcher, listener, pwd, EnvVarsUtil.override(environment, getEnvironment()), args);
    }

    public boolean buildout(Launcher launcher, TaskListener listener, Workspace workspace, FilePath pwd,
                            EnvVars environment, String buildoutCfg) throws InterruptedException, IOException {
        // Get the environment
        EnvVars finalEnvironment = EnvVarsUtil.override(environment, getEnvironment());
        // Create the arguments for the command line
        ArgumentListBuilder args = new ArgumentListBuilder();
        // Add the path to bootstrap executable
        args.add(getExecutable("buildout").getRemote());
        args.add("bootstrap");
        // Add the configuration
        args.add("-c").add(buildoutCfg);
        // Start bootstrap
        if (!LauncherUtil.launch(launcher, listener, pwd, finalEnvironment, args))
            // Failed to bootstrap, no need to go further
            return false;
        // Call BUILDOUT and return status
        return LauncherUtil.launch(launcher, listener, pwd, finalEnvironment, new ArgumentListBuilder(getExecutable("buildout").getRemote(), "-c", buildoutCfg));
    }

    public static String getSignature(Workspace workspace, Python interpreter, boolean systemSitePackages)
            throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        // Get the executable
        FilePath executable = interpreter.getExecutable();
        // Add the path to executable
        sb.append(executable.getRemote()).append("\n");
        // Add the executable MD5
        sb.append(executable.digest()).append("\n");
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
