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

import java.io.IOException;

import jenkins.plugins.shiningpanda.util.FilePathUtil;

public class SlaveWorkspace extends Workspace
{

    /**
     * Folder on executor containing packages provided by user to avoid
     * downloads when creating a VIRTUALENV.
     */
    private FilePath packages;

    /**
     * Constructor using fields.
     * 
     * @param home
     *            The home folder of the workspace.
     */
    public SlaveWorkspace(FilePath home)
    {
        // Call super
        super(home);
        // Compute and store the packages folder
        packages = new FilePath(getCache(), "packages");
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.workspace.Workspace#getVirtualenvPy()
     */
    @Override
    public FilePath getVirtualenvPy() throws IOException, InterruptedException
    {
        return FilePathUtil.synchronize(getMasterVirtualenvPy(), new FilePath(getCache(), VIRTUALENV));
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.workspace.Workspace#getPackagesDir()
     */
    @Override
    public FilePath getPackagesDir() throws IOException, InterruptedException
    {
        return FilePathUtil.isDirectoryOrNull(FilePathUtil.synchronize(getMasterPackagesDir(), packages));
    }
}
