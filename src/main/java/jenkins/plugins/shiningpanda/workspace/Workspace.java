/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011 ShiningPanda S.A.S.
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
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.IOException;

import jenkins.model.Jenkins;
import jenkins.plugins.shiningpanda.util.FilePathUtil;

public abstract class Workspace
{

    /**
     * Name of the VIRTUALENV module.
     */
    protected static String VIRTUALENV = "virtualenv.py";

    /**
     * Home folder for the workspace.
     */
    private FilePath home;

    /**
     * ShiningPanda cache folder in the workspace.
     */
    private FilePath cache;

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
        // Store ShiningPanda cache folder
        setCache(new FilePath(home, ".shiningpanda"));
    }

    /**
     * Get workspace's home folder
     * 
     * @return The home folder
     */
    public FilePath getHome()
    {
        return home;
    }

    /**
     * Set workspace's home folder.
     * 
     * @param home
     *            The home folder
     */
    private void setHome(FilePath home)
    {
        this.home = home;
    }

    /**
     * Get workspace's cache folder.
     * 
     * @return The cache folder
     */
    public FilePath getCache()
    {
        return cache;
    }

    /**
     * Set workspace's cache folder.
     * 
     * @param cache
     *            The cache folder
     */
    private void setCache(FilePath cache)
    {
        this.cache = cache;
    }

    /**
     * Is this an UNIX workspace?
     * 
     * @return true if an UNIX workspace, else false
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean isUnix() throws IOException, InterruptedException
    {
        return FilePathUtil.isUnix(getHome());
    }

    /**
     * Is this a Windows workspace?
     * 
     * @return true if a Windows workspace, else false
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean isWindows() throws IOException, InterruptedException
    {
        return FilePathUtil.isWindows(getHome());
    }

    /**
     * Get the VIRTUALENV module file on master.
     * 
     * @return The VIRTUALENV module file
     */
    protected FilePath getMasterVirtualenvPy()
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
    protected FilePath getMasterPackagesDir() throws IOException, InterruptedException
    {
        return FilePathUtil.isDirectoryOrNull(Jenkins.getInstance().getRootPath().child("shiningpanda").child("packages"));
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
     * @return The VIRTUALENV
     */
    public FilePath getVirtualenvHome()
    {
        return new FilePath(getCache(), "env");
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
        return fromHome(build.getWorkspace());
    }
}
