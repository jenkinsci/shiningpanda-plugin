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
package jenkins.plugins.shiningpanda.workspace;

import hudson.FilePath;
import hudson.Util;
import hudson.matrix.MatrixConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Node;

import java.io.File;
import java.io.IOException;

import jenkins.model.Jenkins;
import jenkins.plugins.shiningpanda.utils.FilePathUtil;

public abstract class Workspace
{

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
     * Get the VIRTUALENV home for this workspace, where TOX (or other tools)
     * can be installed for instance.
     * 
     * @return The VIRTUALENV home
     */
    public FilePath getVirtualenvHome(String name)
    {
        return getHome().child("virtualenvs").child(Util.getDigestOf(Util.fixNull(name)).substring(0, 8));
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
        return fromNode(build.getBuiltOn(), build.getProject());
    }

    /**
     * Create a workspace from the node and the project.
     * 
     * @param node
     *            The node
     * @param project
     *            The project
     * @return The workspace
     */
    public static Workspace fromNode(Node node, AbstractProject<?, ?> project)
    {
        // Get the name of the project as identifier
        String id = project.getName();
        // Check if this is the child of a matrix project
        if (project instanceof MatrixConfiguration)
            // If it is, also add the name of the parent project
            id += ((MatrixConfiguration) project).getParent().getName();
        // Get the home folder of this workspace
        FilePath work = node.getRootPath().child(BASENAME).child("jobs").child(Util.getDigestOf(id).substring(0, 8));
        // Build the workspace from home
        return fromHome(work);
    }
}
