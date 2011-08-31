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
package jenkins.plugins.shiningpanda;

import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;

public abstract class InstalledPythonBuildStepDescriptor extends BuildStepDescriptor<Builder>
{

    /**
     * Name of the default PYTHON
     */
    public final static String defaultInstallationName = PythonInstallation.defaultInstallationName;

    /**
     * Flag to determine if PYTHON selection is let to user (useful for build
     * matrix)
     */
    public volatile boolean showInstallations;

    /**
     * Default constructor
     */
    public InstalledPythonBuildStepDescriptor()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
     */
    @Override
    public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType)
    {
        // Set the flag (dirty to do this here, but do not know where to do it)
        showInstallations = !jobType.equals(MatrixProject.class);
        // Always applicable
        return true;
    }

    /**
     * Get all the PYTHON installations
     * 
     * @return An array of PYTHON installations
     */
    public StandardPythonInstallation[] getInstallations()
    {
        return ToolInstallation.all().get(StandardPythonInstallation.DescriptorImpl.class).getInstallations();
    }

}
