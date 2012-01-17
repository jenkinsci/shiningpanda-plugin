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

import java.io.IOException;

import javax.servlet.ServletException;

import jenkins.plugins.shiningpanda.publishers.CoveragePublisher;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class CoverageBuildAction extends CoverageAction
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
    public CoverageBuildAction(AbstractBuild<?, ?> build)
    {
        // Call super
        super();
        // Store the build
        this.build = build;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.actions.coverage.CoverageAction#show()
     */
    protected boolean show()
    {
        // Delegate
        return hasReports(CoveragePublisher.getHtmlDir(build));
    }

    /**
     * Serve report files.
     * 
     * @param req
     *            The request
     * @param rsp
     *            The response
     * @throws IOException
     * @throws ServletException
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
        // Delegate
        serve(req, rsp, build.getFullDisplayName(), CoveragePublisher.getHtmlDir(build), getPath(req));
    }
}
