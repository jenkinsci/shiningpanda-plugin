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

public class MasterWorkspace extends Workspace
{

    /**
     * Constructor using fields.
     * 
     * @param home
     *            The home folder of the workspace.
     */
    public MasterWorkspace(FilePath home)
    {
        super(home);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.workspace.Workspace#getVirtualenvPy()
     */
    @Override
    public FilePath getVirtualenvPy()
    {
        return getMasterVirtualenvPy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.workspace.Workspace#getPackagesDir()
     */
    @Override
    public FilePath getPackagesDir() throws IOException, InterruptedException
    {
        return getMasterPackagesDir();
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.workspace.Workspace#getBootstrapPy()
     */
    @Override
    public FilePath getBootstrapPy() throws IOException, InterruptedException
    {
        return getMasterBootstrapPy();
    }

}
