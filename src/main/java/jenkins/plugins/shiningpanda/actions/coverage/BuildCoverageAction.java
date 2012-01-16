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

import hudson.model.AbstractBuild;

import java.io.File;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.publishers.CoveragePublisher;

public class BuildCoverageAction extends CoverageAction
{

    /**
     * The build.
     */
    private final AbstractBuild<?, ?> build;

    /**
     * Constructor using fields.
     * 
     * @param build
     *            The build
     */
    public BuildCoverageAction(AbstractBuild<?, ?> build)
    {
        // Call super
        super();
        // Store the build
        this.build = build;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.actions.coverage.CoverageAction#getTitle()
     */
    protected String getTitle()
    {
        return Messages.CoverageAction_Title() + " - " + build.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.shiningpanda.actions.coverage.CoverageAction#getDir()
     */
    protected File getDir()
    {
        return CoveragePublisher.getHtmlDir(build);
    }
}