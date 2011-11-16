package jenkins.plugins.shiningpanda.scm;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogParser;
import hudson.scm.NullChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.scm.NullSCM;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ToxSCM extends NullSCM
{

    /**
     * Path to the tox.ini file
     */
    private String toxIni;

    /**
     * Content of the tox.ini file
     */
    private String content;

    /**
     * Constructor using fields
     * 
     * @param toxIni
     *            The tox.init file
     * @param content
     *            The tox.ini content
     */
    public ToxSCM(String toxIni, String content)
    {
        super();
        this.toxIni = toxIni;
        this.content = content;
    }

    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException
    {
        return null;
    }

    protected PollingResult compareRemoteRevisionWith(@SuppressWarnings("rawtypes") AbstractProject project, Launcher launcher,
            FilePath workspace, TaskListener listener, SCMRevisionState baseline) throws IOException, InterruptedException
    {
        return PollingResult.NO_CHANGES;
    }

    public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath remoteDir, BuildListener listener,
            File changeLogFile) throws IOException, InterruptedException
    {
        FilePath toxIniFp = build.getWorkspace().child(toxIni);
        FilePath setupPyFp = toxIniFp.getParent().child("setup.py");
        FileUtils.writeStringToFile(new File(toxIniFp.getRemote()), content);
        FileUtils.writeStringToFile(new File(setupPyFp.getRemote()), "from setuptools import setup; setup();");
        return createEmptyChangeLog(changeLogFile, listener, "log");
    }

    public ChangeLogParser createChangeLogParser()
    {
        return new NullChangeLogParser();
    }

    @Extension
    public static class DescriptorImpl extends SCMDescriptor<NullSCM>
    {
        public DescriptorImpl()
        {
            super(null);
        }

        public String getDisplayName()
        {
            return getClass().getName();
        }
    }
}