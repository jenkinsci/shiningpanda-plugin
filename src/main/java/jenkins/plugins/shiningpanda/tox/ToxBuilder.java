/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011 ShiningPanda S.A.S.
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
package jenkins.plugins.shiningpanda.tox;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.StandardPythonInstallation;

import org.kohsuke.stapler.DataBoundConstructor;

public class ToxBuilder extends Builder implements Serializable
{

    /**
     * Path to the tox.ini file
     */
    public final String toxIni;

    /**
     * Positional arguments: [] in tox.ini will be replace bay these arguments
     */
    public final String posArgs;

    /**
     * Force recreation of virtual environments
     */
    public final boolean recreate;

    @DataBoundConstructor
    public ToxBuilder(String toxIni, String posArgs, boolean recreate)
    {
        // Call super
        super();
        // Store the path to the tox.ini file
        this.toxIni = Util.fixEmptyAndTrim(toxIni);
        // Store positional arguments
        this.posArgs = Util.fixEmptyAndTrim(posArgs);
        // Store the recreation flag
        this.recreate = recreate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild
     * , hudson.Launcher, hudson.model.BuildListener)
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException
    {
        return perform(build, launcher, (TaskListener) listener);
    }

    /**
     * Perform the build
     * 
     * @param build
     *            The build
     * @param launcher
     *            The task launcher
     * @param listener
     *            The listener
     * @return Return true if the build went well
     * @throws InterruptedException
     */
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws InterruptedException
    {
        FilePath ws = build.getWorkspace();
        int r;
        try
        {
            // Get the environment variables for this build
            EnvVars envVars = build.getEnvironment(listener);
            // Add build variables, such as the user defined text axis for
            // matrix builds
            for (Map.Entry<String, String> e : build.getBuildVariables().entrySet())
                // Add the variable
                envVars.put(e.getKey(), e.getValue());
            // Set the environment of this specific builder
            setEnvironment(envVars, build, Computer.currentComputer().getNode(), launcher, listener);
            // Start command line
            r = launcher.launch().cmds(buildCommandLine()).envs(envVars).stdout(listener).pwd(ws).join();
        }
        catch (IOException e)
        {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("pas cool"));//"Messages.CommandInterpreter_CommandFailed()));
            r = -1;
        }
        return r == 0;
    }

    /**
     * Build the command line to call TOX
     * 
     * @return The command line
     */
    public List<String> buildCommandLine()
    {
        // Initialize the command line
        List<String> args = new ArrayList<String>();
        // Add the TOX executable
        args.add("tox");
        // Add the configuration file option
        args.add("-c");
        // Add the configuration file
        args.add(toxIni);
        // Check if force recreation of virtual environments
        if (recreate)
            // If yes, add recreate option
            args.add("--recreate");
        // Check for some positional arguments
        if (posArgs != null)
        {
            // Add the double dash separation
            args.add("--");
            // Add positional arguments
            args.addAll(Arrays.asList(Util.tokenize(posArgs)));
        }
        // Return the command line
        return args;
    }

    protected void setEnvironment(EnvVars envVars, AbstractBuild<?, ?> build, Node node, Launcher launcher,
            TaskListener listener) throws InterruptedException, IOException
    {
        // Get the list of installations

        List<StandardPythonInstallation> pis = Arrays.asList(((DescriptorImpl) getDescriptor()).getInstallations());

        Collections.reverse(pis);

        for (StandardPythonInstallation pi : pis)
        {
            // Configure for the node
            pi = pi.forNode(node, listener);
            // Configure for the environment
            pi = pi.forEnvironment(envVars);

            pi.setEnvironment(envVars, getPathSeparator(launcher));

        }
    }

    /**
     * Get the path separator of the node where the build runs
     * 
     * @param launcher
     *            The task launcher
     * @return The remote path separator
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("serial")
    public String getPathSeparator(Launcher launcher) throws IOException, InterruptedException
    {
        return launcher.getChannel().call(new Callable<String, IOException>()
        {
            public String call() throws IOException
            {
                return File.pathSeparator;
            }
        });
    }

    private static final long serialVersionUID = 1L;

    /**
     * Descriptor for this builder
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
    {
        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName()
        {
            return Messages.ToxBuilder_DisplayName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile()
        {
            return "/plugin/shiningpanda/help/ToxBuilder/help.html";
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
         */
        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType)
        {
            // Only available in matrix projects if some installations exist
            return getInstallations().length != 0 && jobType.equals(MatrixProject.class);
        }

        /**
         * Get all the PYTHON installations
         * 
         * @return An array of PYTHON installations
         */
        public StandardPythonInstallation[] getInstallations()
        {
            return ToolInstallation.all().get(StandardPythonInstallation.DescriptorImpl.class).getInstallations();
        }
    }
}
