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
package jenkins.plugins.shiningpanda.workspace;

import hudson.FilePath;
import hudson.Util;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.Project;
import jenkins.model.Jenkins;
import jenkins.plugins.shiningpanda.utils.FilePathUtil;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Workspace {
    private static final Logger LOGGER = Logger.getLogger(Workspace.class.getName());
    public static final String BASENAME = "shiningpanda";
    public static final String PACKAGES = "packages";
    protected static final String VIRTUALENV = "virtualenv.py";
    protected static final String SETUPTOOLS = "setuptools-0-py2.py3-none-any.whl";
    protected static final String PIP = "pip-0-py2.py3-none-any.whl";
    protected static final String WHEEL = "wheel-0-py2.py3-none-any.whl";
    protected static final String BOOTSTRAP = "bootstrap.py";
    private FilePath home;

    protected Workspace(FilePath home) {
        // Call super
        super();
        // Store home folder
        setHome(home);
    }

    public FilePath getHome() {
        return home;
    }

    private void setHome(FilePath home) {
        this.home = home;
    }

    private String getVirtualenvPyContent() throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(VIRTUALENV), StandardCharsets.UTF_8);
    }

    private FilePath getVirtualenvPy() throws IOException, InterruptedException {
        // TODO: optimize transfer of PIP, SETUPTOOLS, WHEEL
        getHome().child(SETUPTOOLS).copyFrom(getClass().getResource(SETUPTOOLS));
        getHome().child(PIP).copyFrom(getClass().getResource(PIP));
        getHome().child(WHEEL).copyFrom(getClass().getResource(WHEEL));
        return FilePathUtil.synchronize(getHome().child(VIRTUALENV), getVirtualenvPyContent());
    }

    private String getBootstrapPyContent() throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(BOOTSTRAP), StandardCharsets.UTF_8);
    }

    private FilePath getBootstrapPy() throws IOException, InterruptedException {
        return FilePathUtil.synchronize(getHome().child(BOOTSTRAP), getBootstrapPyContent());
    }

    public FilePath getMasterPackagesDir() throws IOException, InterruptedException {
        return FilePathUtil.isDirectoryOrNull(Jenkins.get().getRootPath().child(BASENAME).child(PACKAGES));
    }

    public abstract FilePath getPackagesDir() throws IOException, InterruptedException;

    public FilePath getToolsHome() {
        return getHome().child("tools");
    }

    public FilePath getVirtualenvHome(String name) {
        return getHome().child("virtualenvs").child(Util.getDigestOf(Util.fixNull(name)).substring(0, 8));
    }

    public FilePath getBuildoutHome(String name) {
        return getHome().child("buildouts").child(Util.getDigestOf(Util.fixNull(name)).substring(0, 8));
    }

    protected void delete() {
        // Get errors
        try {
            // Delete recursively
            getHome().deleteRecursive();
        } catch (Exception e) {
            // Log
            LOGGER.log(Level.SEVERE, "Failed to delete workspace: " + getHome().getRemote(), e);
        }
    }

    public static Workspace fromHome(FilePath home) {
        return home.isRemote() ? new SlaveWorkspace(home) : new MasterWorkspace(home);
    }

    public static Workspace fromBuild(AbstractBuild<?, ?> build) {
        return fromNode(build.getBuiltOn(), build.getProject(), null);
    }

    public static Workspace fromProject(Project<?, ?> project, String name) {
        return fromNode(project.getLastBuiltOn(), project, name);
    }

    public static Workspace fromNode(Node node, AbstractProject<?, ?> project, String name) {
        // Check if node exists
        if (node == null)
            // Unable to get the workspace
            return null;
        // Get the name of the project as identifier
        String id;
        // Check if this is the child of a matrix project
        if (project instanceof MatrixConfiguration)
            // Append the name of the parent project or the provided name if
            // exists with the project name
            id = (name != null ? name : ((MatrixConfiguration) project).getParent().getName()) + project.getName();
            // This is a standard project
        else
            // Use the name of the project or the provided name if exists
            id = name != null ? name : project.getName();
        // Build the workspace from home
        return fromHome(WorkspaceHomeProperty.get(node).child(Util.getDigestOf(id).substring(0, 8)));
    }

    public static void delete(Item item) {
        // Delegate
        delete(item, null);
    }

    public static void delete(Item item, String name) {
        // Check if this is a matrix project
        if (item instanceof MatrixProject)
            // Go threw the configurations
            for (MatrixConfiguration configuration : ((MatrixProject) item).getItems()) {
                // Get workspace
                Workspace workspace = fromProject(configuration, name);
                // Check if exists
                if (workspace != null)
                    // Delete it
                    workspace.delete();
            }
            // Check if this is a real project
        else if (item instanceof Project) {
            // Get workspace
            Workspace workspace = fromProject((Project<?, ?>) item, name);
            // Check if exists
            if (workspace != null)
                // Delete it
                workspace.delete();
        }
    }

}
