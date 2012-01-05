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
package jenkins.plugins.shiningpanda.actions.coverage;

import hudson.model.ProminentProjectAction;
import hudson.model.AbstractProject;
import hudson.model.Run;

import java.io.File;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.recorders.CoverageArchiver;

public class ProjectCoverageAction extends CoverageAction implements ProminentProjectAction
{

    /**
     * The project.
     */
    private final AbstractProject<?, ?> project;

    /**
     * Constructor using fields.
     * 
     * @param project
     *            The project
     */
    public ProjectCoverageAction(AbstractProject<?, ?> project)
    {
        // Call super
        super();
        // Store the project
        this.project = project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.actions.coverage.CoverageAction#getTitle()
     */
    protected String getTitle()
    {
        return Messages.CoverageAction_Title() + " - " + project.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.actions.coverage.CoverageAction#getDir()
     */
    protected File getDir()
    {
        // Get the last successful build
        Run<?, ?> run = project.getLastSuccessfulBuild();
        // Check if found a successful one
        if (run != null)
        {
            // Get the coverage folder for the last successful
            File dir = CoverageArchiver.getHtmlDir(run);
            // Check if this folder exists
            if (dir.exists())
                // If exists return it
                return dir;
        }
        // Else get it for the project
        return CoverageArchiver.getHtmlDir(project);
    }
}