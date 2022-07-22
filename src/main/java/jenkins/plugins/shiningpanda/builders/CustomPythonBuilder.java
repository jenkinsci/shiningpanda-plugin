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
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.command.CommandNature;
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.utils.BuilderUtil;
import jenkins.plugins.shiningpanda.utils.FormValidationUtil;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class CustomPythonBuilder extends Builder implements Serializable {
    public final String home;
    public final String nature;
    public final String command;
    public final boolean ignoreExitCode;

    @DataBoundConstructor
    public CustomPythonBuilder(String home, String nature, String command, boolean ignoreExitCode) {
        // Call super
        super();
        // Store the home directory
        this.home = home;
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
        // Get the interpreter
        Python interpreter = BuilderUtil.getInterpreter(launcher, listener, environment.expand(home));
        // Check if got an interpreter
        if (interpreter == null)
            // Failed to get the interpreter, no need to go further
            return false;
        // Launch script
        return BuilderUtil.launch(launcher, listener, build.getWorkspace(), environment, interpreter, nature, command,
                ignoreExitCode);
    }

    private static final long serialVersionUID = 1L;

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public String getDisplayName() {
            return Messages.CustomPythonBuilder_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return Functions.getResourcePath() + "/plugin/shiningpanda/help/builders/CustomPythonBuilder/help.html";
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            // Always available
            return true;
        }

        public FormValidation doCheckHome(@SuppressWarnings("rawtypes") @AncestorInPath AbstractProject project,
                                          @QueryParameter String value) {
            // This can be used to check the existence of a file on the
            // server, so needs to be protected
            if (!project.hasPermission(Item.CONFIGURE))
                // Do not validate
                return FormValidation.ok();
            // Validate PYTHON home
            return FormValidationUtil.validatePython(value);
        }

        public List<CommandNature> getNatures() {
            return CommandNature.ALL;
        }
    }
}
