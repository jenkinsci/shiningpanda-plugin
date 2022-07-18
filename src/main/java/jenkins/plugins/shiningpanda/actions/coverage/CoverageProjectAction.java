/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2015 ShiningPanda S.A.S.
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

import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;
import jenkins.plugins.shiningpanda.publishers.CoveragePublisher;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoverageProjectAction extends CoverageAction implements ProminentProjectAction {

    /**
     * The project.
     */
    private final AbstractProject<?, ?> project;

    /**
     * Constructor using fields.
     *
     * @param project The project
     */
    public CoverageProjectAction(AbstractProject<?, ?> project) {
        // Call super
        super();
        // Store the project
        this.project = project;
    }

    /**
     * Check if this is a matrix project.
     *
     * @return The cast project is this is a matrix project
     */
    private MatrixProject isMatrix() {
        // Check if this is a matrix project
        if (project instanceof MatrixProject)
            // Cast it
            return (MatrixProject) project;
        // Else return null
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see jenkins.plugins.shiningpanda.actions.coverage.CoverageAction#show()
     */
    protected boolean show() {
        // Check if this is a matrix project
        MatrixProject matrix = isMatrix();
        // Actually check
        if (matrix == null)
            // Check if some report exists for the last successful build
            return hasReports(CoveragePublisher.getHtmlDir(project.getLastSuccessfulBuild()));
        // Go threw the configurations
        for (MatrixConfiguration configuration : matrix.getActiveConfigurations())
            // Check if this configuration has a report
            if (hasReports(CoveragePublisher.getHtmlDir(configuration.getLastSuccessfulBuild())))
                // If yes no need to go further
                return true;
        // No report found
        return false;
    }

    /**
     * Serve report files.
     *
     * @param req The request
     * @param rsp The response
     * @throws IOException
     * @throws ServletException
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        // First of all set this flag to avoid cache as this report may change
        // quickly
        rsp.addHeader("Cache-Control", "no-cache");
        // Get the path
        String path = getPath(req);
        // Check if this is a matrix project
        MatrixProject matrix = isMatrix();
        // Check if this is a matrix project
        if (matrix == null) {
            // This is not, get the last successful build
            Run<?, ?> run = project.getLastSuccessfulBuild();
            // Serve the report
            serve(req, rsp, run.getFullDisplayName(), CoveragePublisher.getHtmlDir(run), path);
            // No need to go further
            return;
        }
        // Store the configurations with reports
        List<MatrixConfiguration> configurations = new ArrayList<MatrixConfiguration>();
        // Go threw the active configurations
        for (MatrixConfiguration configuration : matrix.getActiveConfigurations())
            // Check is has some reports
            if (hasReports(CoveragePublisher.getHtmlDir(configuration.getLastSuccessfulBuild())))
                // If has at least one add to the list
                configurations.add(configuration);
        // Check that at least one configuration found
        if (configurations.size() == 0) {
            // If non found, return a 404
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            // No need to go further
            return;
        }
        // Check if only one configuration
        if (configurations.size() == 1) {
            // Get the last successful build for this configuration
            MatrixRun run = configurations.get(0).getLastSuccessfulBuild();
            // Server its report directly
            serve(req, rsp, run.getFullDisplayName(), CoveragePublisher.getHtmlDir(run), path);
            // No need to go further
            return;
        }
        // Check if matrix index required
        if ("/".equals(path)) {
            // Store the index entries
            List<Entry> entries = new ArrayList<Entry>();
            // Go threw the configurations
            for (MatrixConfiguration configuration : configurations) {
                // Get the last successful build for this configuration
                MatrixRun run = configuration.getLastSuccessfulBuild();
                // Create an entry for this configuration
                Entry entry = new Entry(run.getFullDisplayName(), configuration.getCombination().toString() + "/");
                // Get its base report folder
                File base = CoveragePublisher.getHtmlDir(run);
                // If this base report is not a report folder, create a sub item
                // for each report
                if (!isReport(base))
                    // Go threw the reports
                    for (String dir : getReports(base))
                        // Add a child item
                        entry.children.add(new Entry(dir, configuration.getCombination().toString() + "/" + dir + "/"));
                // Add the entry to the list
                entries.add(entry);
            }
            // Set page title in context
            req.setAttribute("title", matrix.getFullDisplayName());
            // Set entries in context
            req.setAttribute("entries", entries);
            // Render the view
            req.getView(CoverageProjectAction.class, "entries.jelly").forward(req, rsp);
            // No need to go further
            return;
        }
        // Analyze the query
        String[] parts = path.split("/", 3);
        // Get the configuration from the first part of the URL
        MatrixConfiguration configuration = matrix.getItem(parts[1]);
        // Check if found a configuration
        if (configuration == null) {
            // If not, return a 404
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            // No need to go further
            return;
        }
        // Get the remaining path without the configuration prefix
        path = parts.length <= 2 ? "/" : "/" + parts[2];
        // Get the last successful build for this configuration
        MatrixRun run = configuration.getLastSuccessfulBuild();
        // Serve the file
        serve(req, rsp, run.getFullDisplayName(), CoveragePublisher.getHtmlDir(run), path);
    }
}
