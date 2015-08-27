/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2014 ShiningPanda S.A.S.
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
import java.util.Collections;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

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
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.interpreters.Virtualenv;
import jenkins.plugins.shiningpanda.matrix.ToxAxis;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import jenkins.plugins.shiningpanda.utils.BuilderUtil;
import jenkins.plugins.shiningpanda.utils.FilePathUtil;
import jenkins.plugins.shiningpanda.utils.UnixVariableResolver;
import jenkins.plugins.shiningpanda.workspace.Workspace;

public class ToxBuilder extends Builder implements Serializable {

    /**
     * Path to the tox.ini file
     */
    public final String toxIni;

    /**
     * Force recreation of virtual environments
     */
    public final boolean recreate;

    /**
     * If there is no TOX axis, use this field to get the TOX environment.
     */
    public final String toxenvPattern;

    /**
     * Constructor using fields.
     * 
     * @param toxIni
     *            The TOX configuration file
     * @param recreate
     *            Create a new environment each time
     * @param toxenvPattern
     *            The pattern used to build the TOXENV environment variable
     */
    @DataBoundConstructor
    public ToxBuilder(String toxIni, boolean recreate, String toxenvPattern) {
	// Call super
	super();
	// Store the path to the tox.ini file
	this.toxIni = Util.fixEmptyAndTrim(toxIni);
	// Store the recreation flag
	this.recreate = recreate;
	// Store the TOXENV pattern
	this.toxenvPattern = Util.fixEmptyAndTrim(toxenvPattern);
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
	// Get the workspace
	Workspace workspace = Workspace.fromBuild(build);
	// Get the environment variables for this build
	EnvVars environment = BuilderUtil.getEnvironment(build, listener);
	// Check if this is a valid environment
	if (environment == null)
	    // Invalid, no need to go further
	    return false;
	// Check if environment contains a TOX axis
	if (!environment.containsKey(ToxAxis.KEY)) {
	    // Check if this builder uses a TOXENV pattern
	    if (toxenvPattern != null) {
		// Get the TOXENV value
		String toxenv = Util
			.fixEmptyAndTrim(Util.replaceMacro(toxenvPattern, new UnixVariableResolver(environment)));
		// Check if this is a valid TOXENV value
		if (toxenv != null)
		    // Set this value
		    environment.put(ToxAxis.KEY, toxenv);
		// Invalid TOXENV value
		else {
		    // Log
		    listener.fatalError(Messages.ToxBuilder_ToxenvPattern_Invalid(toxenvPattern));
		    // No need to go further
		    return false;
		}
	    } else {
		// Log
		listener.fatalError(Messages.ToxBuilder_ToxAxis_Required());
		// No need to go further
		return false;
	    }
	}
	// Check that TOX axis is not used with a TOXENV pattern. If TOXENV
	// pattern equals TOX axis variable, skip this warning as final result
	// is the same
	else if (toxenvPattern != null && toxenvPattern != "$" + ToxAxis.KEY) {
	    // Log that a TOXENV pattern should not be used with a TOX axis
	    listener.fatalError(Messages.ToxBuilder_ToxAxis_And_ToxenvPattern());
	    // No need to go further
	    return false;
	}
	// Get a VIRTUALENV to install TOX
	Virtualenv virtualenv = BuilderUtil.getVirtualenv(listener, workspace.getToolsHome());
	// Check if is a valid one
	if (virtualenv == null)
	    // Invalid, no need to go further
	    return false;
	// Get an interpreter to potentially be able to create the VIRTUALENV
	Python interpreter = BuilderUtil.getInterpreter(launcher, listener, environment);
	// Check if found one
	if (interpreter == null)
	    // No interpreter found, no need to continue
	    return false;
	// Get the working directory
	FilePath pwd = build.getWorkspace();
	// Creation flag for system packages
	boolean systemSitePackages = false;
	// Check if out of date to be able to create a new one
	if (virtualenv.isOutdated(workspace, interpreter, systemSitePackages))
	    // Create the VIRTUALENV
	    if (!virtualenv.create(launcher, listener, workspace, pwd, environment, interpreter, systemSitePackages))
		// Failed to create the VIRTUALENV, do not continue
		return false;
	// Install or upgrade TOX
	if (!virtualenv.pipInstall(launcher, listener, workspace, pwd, environment, "tox"))
	    // Failed to install TOX, do not continue
	    return false;
	// If on UNIX add all PYTHONS in the path so TOX is able to find the
	// right one. Useless on Windows as TOX only looks in standard
	// locations.
	if (FilePathUtil.isUnix(pwd)) {
	    // Get all the available interpreters on the executor
	    List<Python> contributors = BuilderUtil.getInterpreters(launcher, listener, environment);
	    // Reverse the order to be able to sort the environment variables
	    Collections.reverse(contributors);
	    // Go threw the interpreters to add them in the path
	    for (Python contributor : contributors)
		// Add the environment without the home variables
		environment.overrideAll(contributor.getEnvironment(false));
	}
	// Launch TOX
	return virtualenv.tox(launcher, listener, pwd, environment, toxIni, recreate);
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
	    return Messages.ToxBuilder_DisplayName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Descriptor#getHelpFile()
	 */
	@Override
	public String getHelpFile() {
	    return Functions.getResourcePath() + "/plugin/shiningpanda/help/builders/ToxBuilder/help.html";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
	 */
	@Override
	public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
	    // Only available in matrix projects if some installations exist
	    return !PythonInstallation.isEmpty() && jobType.equals(MatrixProject.class);
	}

	/**
	 * Checks if the TOX configuration file is specified.
	 * 
	 * @param value
	 *            The value to check
	 * @return The validation result
	 */
	public FormValidation doCheckToxIni(@QueryParameter String value) {
	    // Check that path is specified
	    if (Util.fixEmptyAndTrim(value) == null)
		// A tox.ini file is required
		return FormValidation.error(Messages.ToxBuilder_ToxIni_Required());
	    // Do not need to check more as files are located on slaves
	    return FormValidation.ok();
	}
    }
}
