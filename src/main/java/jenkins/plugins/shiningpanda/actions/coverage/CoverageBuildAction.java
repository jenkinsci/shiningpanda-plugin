/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2014 ShiningPanda S.A.S.
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
