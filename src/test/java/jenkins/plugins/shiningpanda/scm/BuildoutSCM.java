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
package jenkins.plugins.shiningpanda.scm;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.NullChangeLogParser;
import hudson.scm.NullSCM;
import hudson.scm.PollingResult;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class BuildoutSCM extends NullSCM {

    /**
     * Path to the buildout.cfg file
     */
    private String buildoutCfg;

    /**
     * Content of the buildout.cfg file
     */
    private String buildoutContent;

    /**
     * Name of the fake DJANGO project
     */
    private String djangoProject;

    /**
     * Constructor using fields
     *
     * @param buildoutCfg     The buildout.cfg file
     * @param buildoutContent The buildout.cfg content
     * @param djangoProject   Name of the fake DJANGO project
     */
    public BuildoutSCM(String buildoutCfg, String buildoutContent, String djangoProject) {
        super();
        this.buildoutCfg = buildoutCfg;
        this.buildoutContent = buildoutContent;
        this.djangoProject = djangoProject;
    }

    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        return null;
    }

    protected PollingResult compareRemoteRevisionWith(@SuppressWarnings("rawtypes") AbstractProject project,
                                                      Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState baseline)
            throws IOException, InterruptedException {
        return PollingResult.NO_CHANGES;
    }

    public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath remoteDir, BuildListener listener,
                            File changeLogFile) throws IOException, InterruptedException {
        FilePath filePath = build.getWorkspace().child(buildoutCfg);
        FileUtils.writeStringToFile(new File(filePath.getRemote()), buildoutContent);
        FilePath djangoDir = build.getWorkspace().child(djangoProject);
        djangoDir.mkdirs();
        FileUtils.writeStringToFile(new File(djangoDir.child("__init__.py").getRemote()), "");
        FileUtils.writeStringToFile(new File(djangoDir.child("settings.py").getRemote()), "");
        return createEmptyChangeLog(changeLogFile, listener, "log");
    }

    public ChangeLogParser createChangeLogParser() {
        return new NullChangeLogParser();
    }

    @Extension
    public static class DescriptorImpl extends SCMDescriptor<NullSCM> {
        public DescriptorImpl() {
            super(null);
        }

        public String getDisplayName() {
            return getClass().getName();
        }
    }
}
