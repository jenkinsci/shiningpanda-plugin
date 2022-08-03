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
package jenkins.plugins.shiningpanda.builders;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.command.CommandNature;
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.interpreters.Virtualenv;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import jenkins.plugins.shiningpanda.utils.BuilderUtil;
import jenkins.plugins.shiningpanda.workspace.Workspace;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class BuildoutBuilder extends Builder implements Serializable {
    public final String pythonName;
    public final String buildoutCfg;
    @Deprecated
    public transient Boolean useDistribute;
    public final String nature;
    public final String command;
    public final boolean ignoreExitCode;

    @DataBoundConstructor
    public BuildoutBuilder(String pythonName, String buildoutCfg, String nature, String command,
                           boolean ignoreExitCode) {
        // Call super
        super();
        // Store the name of the PYTHON to invoke
        this.pythonName = pythonName;
        // Store the path to the tox.ini file
        this.buildoutCfg = Util.fixEmptyAndTrim(buildoutCfg);
        // Store the nature of the command
        this.nature = nature;
        // Normalize and store the command
        this.command = command;
        // Store the ignore flag
        this.ignoreExitCode = ignoreExitCode;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        // Get the workspace
        Workspace workspace = Workspace.fromBuild(build);
        // Get the environment variables for this build
        EnvVars environment = BuilderUtil.getEnvironment(build, listener);
        // Check if this is a valid environment
        if (environment == null)
            // Invalid, no need to go further
            return false;
        // Get the PYTHON installation to use
        PythonInstallation installation = BuilderUtil.getInstallation(build, listener, environment, pythonName);
        // Check if an installation was found
        if (installation == null)
            // If not installation found, do not continue the build
            return false;
        // Get the PYTHON
        Python interpreter = BuilderUtil.getInterpreter(launcher, listener, installation.getHome());
        // Check if found an interpreter
        if (interpreter == null)
            // If no interpreter found, no need to continue
            return false;
        // Create a VIRTUALENV
        Virtualenv virtualenv = BuilderUtil.getVirtualenv(listener, workspace.getBuildoutHome(installation.getName()));
        // Check if this is a valid VIRTUALENV
        if (virtualenv == null)
            // Invalid VIRTUALENV, do not continue
            return false;
        // Get the working directory
        FilePath pwd = build.getWorkspace();
        // Check if clean required or if configuration changed
        if (virtualenv.isOutdated(workspace, interpreter, false))
            // A new environment is required
            if (!virtualenv.create(launcher, listener, workspace, pwd, environment, interpreter, false, true))
                // Failed to create the environment, do not continue
                return false;
        // Install or upgrade Buildout
        if (!virtualenv.pipInstall(launcher, listener, workspace, pwd, environment, "zc.buildout"))
            // Failed to install buildout, do not continue
            return false;
        // Bootstrap
        if (!virtualenv.buildout(launcher, listener, workspace, pwd, environment, buildoutCfg))
            // Failed to bootstrap, no need to continue
            return false;
        // Get the final environment by adding the binary folder in the path
        environment.override("PATH+", pwd.child(buildoutCfg).getParent().child("bin").getRemote());
        // Launch script
        return BuilderUtil.launch(launcher, listener, pwd, environment, virtualenv, nature, command, ignoreExitCode);
    }

    private static final long serialVersionUID = 1L;

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public String getDisplayName() {
            return Messages.BuildoutBuilder_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return Functions.getResourcePath() + "/plugin/shiningpanda/help/builders/BuildoutBuilder/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // If there's no PYTHON configured, there's no point in PYTHON
            // builders
            return !PythonInstallation.isEmpty();
        }

        public boolean isMatrix(Object it) {
            return it instanceof MatrixProject;
        }

        public PythonInstallation[] getInstallations() {
            // Delegate
            return PythonInstallation.list();
        }

        public List<CommandNature> getNatures() {
            return CommandNature.ALL;
        }

        public FormValidation doCheckBuildoutCfg(@QueryParameter String value) {
            // Check that path is specified
            if (Util.fixEmptyAndTrim(value) == null)
                // A buildout.cfg file is required
                return FormValidation.error(Messages.BuildoutBuilder_BuildoutCfg_Required());
            // Do not need to check more as files are located on slaves
            return FormValidation.ok();
        }
    }
}
