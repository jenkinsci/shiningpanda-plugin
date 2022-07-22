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
import hudson.Functions;
import hudson.Launcher;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.command.CommandNature;
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import jenkins.plugins.shiningpanda.utils.BuilderUtil;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class PythonBuilder extends Builder implements Serializable {
    public final String pythonName;
    public final String nature;
    public final String command;
    public final boolean ignoreExitCode;

    @DataBoundConstructor
    public PythonBuilder(String pythonName, String nature, String command, boolean ignoreExitCode) {
        // Call super
        super();
        // Store the name of the PYTHON to invoke
        this.pythonName = pythonName;
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
        // Get the interpreter
        Python interpreter = BuilderUtil.getInterpreter(launcher, listener, installation.getHome());
        // Check if got an interpreter
        if (interpreter == null)
            // If no interpreter found, do not continue the build
            return false;
        // Launch the process
        return BuilderUtil.launch(launcher, listener, build.getWorkspace(), environment, interpreter, nature, command,
                ignoreExitCode);
    }

    private static final long serialVersionUID = 1L;

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public String getDisplayName() {
            return Messages.PythonBuilder_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return Functions.getResourcePath() + "/plugin/shiningpanda/help/builders/PythonBuilder/help.html";
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
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
    }
}
