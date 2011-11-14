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
package jenkins.plugins.shiningpanda.util;

import hudson.EnvVars;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.IOException;

public class BuilderUtil
{

    /**
     * Get the last configuration date of this build.
     * 
     * @param build
     *            The build
     * @return The last configuration date
     */
    public static long lastConfigure(AbstractBuild<?, ?> build)
    {
        return build.getParent().getConfigFile().getFile().lastModified();
    }

    /**
     * Check if this build is for a matrix project.
     * 
     * @param build
     *            The build
     * @return true if this is for a matrix project, else false
     */
    public static boolean isMatrix(AbstractBuild<?, ?> build)
    {
        return build instanceof MatrixRun;
    }

    /**
     * Check if this is a matrix project.
     * 
     * @param jobType
     *            The project
     * @return true if this is a matrix project, else false
     */
    public static boolean isMatrix(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType)
    {
        return jobType.equals(MatrixProject.class);
    }

    /**
     * Get the consolidated environment for the provided build.
     * 
     * @param build
     *            The build
     * @param listener
     *            The build listener
     * @return The consolidated environment
     * @throws IOException
     * @throws InterruptedException
     */
    public static EnvVars getEnvironment(AbstractBuild<?, ?> build, BuildListener listener) throws IOException,
            InterruptedException
    {
        // Get the base environment
        EnvVars environment = build.getEnvironment(listener);
        // Add build variables, for instance if user defined a text axis
        environment.overrideAll(build.getBuildVariables());
        // Return the consolidated environment
        return environment;
    }
}
