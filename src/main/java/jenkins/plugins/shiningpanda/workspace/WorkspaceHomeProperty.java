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
package jenkins.plugins.shiningpanda.workspace;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.Util;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import jenkins.model.Jenkins;
import jenkins.plugins.shiningpanda.Messages;

public class WorkspaceHomeProperty extends NodeProperty<Node> {
    /**
     * The home folder.
     */
    public String home;

    /**
     * Constructor using fields.
     * 
     * @param home
     *            The home folder
     */
    @DataBoundConstructor
    public WorkspaceHomeProperty(String home) {
	// Call super
	super();
	// Store home
	this.home = home;
    }

    /**
     * Get the home folder.
     * 
     * @return The home folder
     */
    public String getHome() {
	return home;
    }

    /**
     * Get the home directory for the given node.
     * 
     * @param node
     *            The node
     * @return The home directory
     */
    public static FilePath get(Node node) {
	// Get the potential properties
	WorkspaceHomeProperty[] properties = new WorkspaceHomeProperty[] {
		node.getNodeProperties().get(WorkspaceHomeProperty.class),
		Jenkins.getInstance().getGlobalNodeProperties().get(WorkspaceHomeProperty.class) };
	// Go threw the properties
	for (WorkspaceHomeProperty property : properties)
	    // Check if exists
	    if (property != null)
		// Check if valid
		if (Util.fixEmpty(property.getHome()) != null)
		    // Return the home folder
		    return new FilePath(node.getChannel(), property.getHome());
	// Else relative to root
	return node.getRootPath().child(Workspace.BASENAME).child("jobs");
    }

    @Extension
    public static class WorkspaceHomePropertyDescriptor extends NodePropertyDescriptor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Descriptor#getHelpFile()
	 */
	@Override
	public String getHelpFile() {
	    return Functions.getResourcePath() + "/plugin/shiningpanda/help/workspace/WorkspaceHomeProperty/help.html";
	}

	@Override
	public String getDisplayName() {
	    return Messages.WorkspaceHomeProperty_DisplayName();
	}

    }
}
