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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

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

public class CustomPythonBuilder extends Builder implements Serializable {

    /**
     * Home directory for the VIRTUALENV
     */
    public final String home;

    /**
     * The nature of the command: PYTHON, shell, X shell
     */
    public final String nature;

    /**
     * The command to execute in the PYTHON environment
     */
    public final String command;

    /**
     * Do not consider the build as a failure if any of the commands exits with
     * a non-zero exit code.
     */
    public final boolean ignoreExitCode;

    /**
     * Constructor using fields
     * 
     * @param home
     *            The home directory for VIRTUALENV
     * @param nature
     *            The nature of the command: PYTHON, shell, X shell
     * @param command
     *            The command to execute
     * @param ignoreExitCode
     *            Do not consider the build as a failure if any of the commands
     *            exits with a non-zero exit code
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.
     * AbstractBuild , hudson.Launcher, hudson.model.BuildListener)
     */
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

    /**
     * Descriptor for this builder
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Descriptor#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
	    return Messages.CustomPythonBuilder_DisplayName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Descriptor#getHelpFile()
	 */
	@Override
	public String getHelpFile() {
	    return Functions.getResourcePath() + "/plugin/shiningpanda/help/builders/CustomPythonBuilder/help.html";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
	 */
	@Override
	public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
	    // Always available
	    return true;
	}

	/**
	 * Check if the VIRTUALENV home is valid
	 * 
	 * @param project
	 *            The project for this builder
	 * @param value
	 *            The folder to check
	 * @return The result of the validation
	 */
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

	/**
	 * Get the list of the available command natures.
	 * 
	 * @return The list of natures
	 */
	public List<CommandNature> getNatures() {
	    return CommandNature.ALL;
	}
    }
}
