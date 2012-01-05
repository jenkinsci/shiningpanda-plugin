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

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.recorders.CoverageArchiver;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class CoverageAction implements Action
{

    /**
     * Index file.
     */
    private final static String INDEX = "index.html";

    /*
     * (non-Javadoc)
     * 
     * @see hudson.model.Action#getDisplayName()
     */
    public String getDisplayName()
    {
        return Messages.CoverageAction_DisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see hudson.model.Action#getUrlName()
     */
    public String getUrlName()
    {
        return CoverageArchiver.BASENAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hudson.model.Action#getIconFileName()
     */
    public String getIconFileName()
    {
        // Check if folder exists
        if (getDir().exists())
            // If exists display the link
            return "graph.png";
        // Else hide the link
        return null;
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
        // Generate an index only if the file does not exists
        boolean serveDirIndex = !new File(new File(getDir(), req.getRestOfPath()), INDEX).exists();
        // Serve the files
        new DirectoryBrowserSupport(this, new FilePath(getDir()), getTitle(), "graph.png", serveDirIndex).generateResponse(req,
                rsp, this);
    }

    /**
     * Get the page title.
     * 
     * @return The title
     */
    protected abstract String getTitle();

    /**
     * Get the base directory containing the coverage report.
     * 
     * @return The directory
     */
    protected abstract File getDir();

}