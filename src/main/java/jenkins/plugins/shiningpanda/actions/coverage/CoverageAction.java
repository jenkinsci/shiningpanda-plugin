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

import hudson.Util;
import hudson.model.Action;
import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.publishers.CoveragePublisher;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public abstract class CoverageAction implements Action {
    public String getDisplayName() {
        return Messages.CoverageAction_DisplayName();
    }

    public String getUrlName() {
        return CoveragePublisher.BASENAME;
    }

    public String getIconFileName() {
        // Check if folder exists
        if (show())
            // If exists display the link
            return "graph.png";
        // Else hide the link
        return null;
    }

    protected abstract boolean show();

    protected String getPath(StaplerRequest req) {
        // Get the remaining part of the URL
        String path = req.getRestOfPath();
        // Check if nothing left to normalize it
        if (path.length() == 0)
            // Normalize it
            path = "/";
        // Return the path
        return path;
    }

    protected boolean hasReports(File base) {
        // If not null then check that at least one report
        return base == null ? false : !getReports(base).isEmpty();
    }

    protected List<String> getReports(File base) {
        // Store reports
        List<String> dirs = new ArrayList<String>();
        // Check is base folder was specified
        if (base == null)
            // If not return the empty list
            return dirs;
        // Check if the base folder is itself a report folder
        if (isReport(base))
            // If yes, return it
            dirs.add("");
            // Else look for multiple reports
        else {
            // Get the file set to search reports
            FileSet fs = Util.createFileSet(base, "**/" + CoveragePublisher.JS, null);
            // Get a scanner
            DirectoryScanner ds = fs.getDirectoryScanner(new Project());
            // Go threw the found files
            for (String file : ds.getIncludedFiles())
                // Check if this is a report
                if (isReport(new File(base, file).getParentFile()))
                    // If this is one, add to the list
                    dirs.add(new File(file).getParent());
        }
        // Return the list of relative paths to reports
        return dirs;
    }

    protected void serve(StaplerRequest req, StaplerResponse rsp, String title, File base, String path)
            throws IOException, ServletException {
        // Check if base folder is specified
        if (base == null) {
            // If not send a 404
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            // No need to go further
            return;
        }
        // Check if an index generation is required
        if (!isReport(base) && "/".equals(path)) {
            // Get the reports
            List<String> dirs = getReports(base);
            // Create a list of bean to generate page
            List<Entry> entries = new ArrayList<Entry>();
            // For each report create an entry
            for (String dir : dirs)
                // Create the entry
                entries.add(new Entry(dir, dir + "/"));
            // Set the title in context
            req.setAttribute("title", title);
            // Set the entries in context
            req.setAttribute("entries", entries);
            // Render the page
            req.getView(CoverageAction.class, "entries.jelly").forward(req, rsp);
            // No need to go further
            return;
        }
        // Serve a file, check that path is valid
        if (path.replace('\\', '/').indexOf("/../") != -1) {
            // If not end a 500
            rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            // No need to go further
            return;
        }
        // Get the file to serve
        File file = new File(base, path.substring(1));
        // Check if this is a folder
        if (file.isDirectory())
            // If this is a folder append an index file
            file = new File(file, CoveragePublisher.INDEX);
        // Check if this file exists
        if (!file.exists()) {
            // If not send a 404
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            // No need to go further
            return;
        }
        // Else serve the file
        rsp.serveFile(req, Files.newInputStream(file.toPath()), file.lastModified(), -1, file.length(), file.getName());
    }

    protected boolean isReport(File base) {
        // Check that not null and that the three file exists
        return base != null && new File(base, CoveragePublisher.INDEX).exists()
                && new File(base, CoveragePublisher.JS).exists()
                && (new File(base, CoveragePublisher.STATUS_LTE_3).exists()
                || new File(base, CoveragePublisher.STATUS).exists());
    }

    public class Entry {
        public List<Entry> children = new ArrayList<Entry>();
        public String title;
        public String target;

        public Entry(String title, String target) {
            // Call super
            super();
            // Store title
            this.title = title;
            // Store target
            this.target = target;
        }
    }
}
