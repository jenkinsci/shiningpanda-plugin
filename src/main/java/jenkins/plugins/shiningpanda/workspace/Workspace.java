/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2013 ShiningPanda S.A.S.
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

import hudson.FilePath;
import hudson.Util;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.Item;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.model.Project;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import jenkins.plugins.shiningpanda.utils.FilePathUtil;

public abstract class Workspace
{

    /**
     * Get a logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Workspace.class.getName());

    /**
     * Base name of the workspace under the node.
     */
    public static String BASENAME = "shiningpanda";

    /**
     * Base name of the folder containing the packages.
     */
    public static String PACKAGES = "packages";

    /**
     * Name of the VIRTUALENV module.
     */
    protected static String VIRTUALENV = "virtualenv.py";

    /**
     * Name of the BUILDOUT bootstrap module.
     */
    protected static String BOOTSTRAP = "bootstrap.py";

    /**
     * Home folder for the workspace.
     */
    private FilePath home;

    /**
     * Constructor using fields.
     * 
     * @param home
     *            The home folder of the workspace.
     */
    public Workspace(FilePath home)
    {
        // Call super
        super();
        // Store home folder
        setHome(home);
    }

    /**
     * Get the home folder of this workspace.
     * 
     * @return The home folder
     */
    public FilePath getHome()
    {
        return home;
    }

    /**
     * Set the home folder for this workspace.
     * 
     * @param home
     *            The home folder
     */
    private void setHome(FilePath home)
    {
        this.home = home;
    }

    /**
     * Get the VIRTUALENV module file on master.
     * 
     * @return The VIRTUALENV module file
     */
    public FilePath getMasterVirtualenvPy()
    {
        return new FilePath(new File(getClass().getResource(VIRTUALENV).getFile()));
    }

    /**
     * Get the VIRTUALENV module file on executor.
     * 
     * @return The VIRTUALENV module file
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract FilePath getVirtualenvPy() throws IOException, InterruptedException;

    /**
     * Get the BUILDOUT bootstrap file on master.
     * 
     * @return The BUILDOUT bootstrap module file
     */
    public FilePath getMasterBootstrapPy()
    {
        return new FilePath(new File(getClass().getResource(BOOTSTRAP).getFile()));
    }

    /**
     * Get the BUILDOUT bootstrap module file on executor.
     * 
     * @return The BUILDOUT bootstrap module file
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract FilePath getBootstrapPy() throws IOException, InterruptedException;

    /**
     * Get the folder on master where user can put some packages to avoid
     * downloads when creating a VIRTUALENV.
     * 
     * @return The packages folder
     * @throws IOException
     * @throws InterruptedException
     */
    public FilePath getMasterPackagesDir() throws IOException, InterruptedException
    {
        return FilePathUtil.isDirectoryOrNull(Jenkins.getInstance().getRootPath().child(BASENAME).child(PACKAGES));
    }

    /**
     * Get the folder on executor containing the packages provided by user to
     * avoid downloads when creating a VIRTUALENV.
     * 
     * @return The packages folder
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract FilePath getPackagesDir() throws IOException, InterruptedException;

    /**
     * Get the VIRTUALENV home for this workspace, where TOX (or other tools)
     * can be installed for instance.
     * 
     * @return The VIRTUALENV home
     */
    public FilePath getToolsHome()
    {
        return getHome().child("tools");
    }

    /**
     * Get the VIRTUALENV home for the provided VIRTUALENV name.
     * 
     * @param name
     *            The name of the VIRTUALENV
     * @return The VIRTUALENV home
     */
    public FilePath getVirtualenvHome(String name)
    {
        return getHome().child("virtualenvs").child(Util.getDigestOf(Util.fixNull(name)).substring(0, 8));
    }

    /**
     * Get the VIRTUALENV home dedicated to BUILDOUT for the provided PYTHON
     * installation name.
     * 
     * @param name
     *            The name of the PYTHON installation
     * @return The VIRTUALENV home
     */
    public FilePath getBuildoutHome(String name)
    {
        return getHome().child("buildouts").child(Util.getDigestOf(Util.fixNull(name)).substring(0, 8));
    }

    /**
     * Delete this workspace without throwing exceptions on error.
     */
    protected void delete()
    {
        // Get errors
        try
        {
            // Delete recursively
            getHome().deleteRecursive();
        }
        catch (Exception e)
        {
            // Log
            LOGGER.log(Level.SEVERE, "Failed to delete workspace: " + getHome().getRemote(), e);
        }
    }

    /**
     * Create the workspace from its home folder.
     * 
     * @param home
     *            The home folder
     * @return The workspace
     */
    public static Workspace fromHome(FilePath home)
    {
        return home.isRemote() ? new SlaveWorkspace(home) : new MasterWorkspace(home);
    }

    /**
     * Create a workspace from the build.
     * 
     * @param build
     *            The build
     * @return The workspace
     */
    public static Workspace fromBuild(AbstractBuild<?, ?> build)
    {
        return fromNode(build.getBuiltOn(), build.getProject(), null);
    }

    /**
     * Get a workspace from a project.
     * 
     * @param project
     *            The project
     * @param name
     *            Base name used to compute the workspace location. If null then
     *            use the name of the project
     * @return The workspace if exists, else null
     */
    public static Workspace fromProject(Project<?, ?> project, String name)
    {
        return fromNode(project.getLastBuiltOn(), project, name);
    }

    /**
     * Create a workspace from the node and the project.
     * 
     * @param node
     *            The node
     * @param project
     *            The project
     * @param name
     *            Base name used to compute the workspace location. If null then
     *            use the name of the project
     * @return The workspace
     */
    public static Workspace fromNode(Node node, AbstractProject<?, ?> project, String name)
    {
        // Check if node exists
        if (node == null)
            // Unable to get the workspace
            return null;
        // Get the name of the project as identifier
        String id;
        // Check if this is the child of a matrix project
        if (project instanceof MatrixConfiguration)
            // Append the name of the parent project or the provided name if
            // exists with the project name
            id = (name != null ? name : ((MatrixConfiguration) project).getParent().getName()) + project.getName();
        // This is a standard project
        else
            // Use the name of the project or the provided name if exists
            id = name != null ? name : project.getName();
        // Build the workspace from home
        return fromHome(WorkspaceHomeProperty.get(node).child(Util.getDigestOf(id).substring(0, 8)));
    }

    /**
     * Clean item related workspaces.
     * 
     * @param item
     *            The item
     */
    public static void delete(Item item)
    {
        // Delegate
        delete(item, null);
    }

    /**
     * Clean item related workspaces.
     * 
     * @param item
     *            The item
     * @param name
     *            The name to use to compute the workspace location
     */
    public static void delete(Item item, String name)
    {
        // Check if this is a matrix project
        if (item instanceof MatrixProject)
            // Go threw the configurations
            for (MatrixConfiguration configuration : ((MatrixProject) item).getItems())
            {
                // Get workspace
                Workspace workspace = fromProject(configuration, name);
                // Check if exists
                if (workspace != null)
                    // Delete it
                    workspace.delete();
            }
        // Check if this is a real project
        else if (item instanceof Project)
        {
            // Get workspace
            Workspace workspace = fromProject((Project<?, ?>) item, name);
            // Check if exists
            if (workspace != null)
                // Delete it
                workspace.delete();
        }
    }

}
