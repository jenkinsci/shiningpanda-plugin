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
package jenkins.plugins.shiningpanda.tools;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import hudson.util.XStream2;
import jenkins.model.Jenkins;
import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.utils.DescriptorUtil;
import jenkins.plugins.shiningpanda.utils.FormValidationUtil;
import jenkins.plugins.shiningpanda.utils.StringUtil;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.List;

public class PythonInstallation extends ToolInstallation
        implements EnvironmentSpecific<PythonInstallation>, NodeSpecific<PythonInstallation> {
    @DataBoundConstructor
    public PythonInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    private static final long serialVersionUID = 1L;

    public PythonInstallation forEnvironment(EnvVars environment) {
        return new PythonInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    public PythonInstallation forNode(Node node, TaskListener listener) throws IOException, InterruptedException {
        return new PythonInstallation(getName(), translateFor(node, listener), getProperties().toList());
    }

    public PythonInstallation forBuild(TaskListener listener, EnvVars environment)
            throws IOException, InterruptedException {
        return forNode(Computer.currentComputer().getNode(), listener).forEnvironment(environment);
    }

    public static boolean isEmpty() {
        return list().length == 0;
    }

    public static PythonInstallation[] list() {
        return ToolInstallation.all().get(DescriptorImpl.class).getInstallations();
    }

    public static PythonInstallation fromName(String name) {
        // Go threw the installations
        for (PythonInstallation installation : list()) {
            // Check if the name match the current installation name
            if (name != null && name.equals(installation.getName()))
                // If yes, we found it
                return installation;
        }
        // No installation matching the provided name
        return null;
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<PythonInstallation> {
        @CopyOnWrite
        private volatile PythonInstallation[] installations = new PythonInstallation[0];

        public DescriptorImpl() {
            // Load saved data on disk (with backward compatibility)
            DescriptorUtil.load(XSTREAM, this, "jenkins.plugins.shiningpanda.StandardPythonInstallation");
        }

        @Override
        public String getHelpFile() {
            return Functions.getResourcePath() + "/plugin/shiningpanda/help/tools/PythonInstallation/help.html";
        }

        @Override
        public String getDisplayName() {
            return Messages.PythonInstallation_DisplayName();
        }

        public PythonInstallation[] getInstallations() {
            return installations;
        }

        public void setInstallations(PythonInstallation... installations) {
            // Store installations
            this.installations = installations;
            // Save on disk
            save();
        }

        public FormValidation doCheckHome(@QueryParameter String value) {
            // This can be used to check the existence of a file on the
            // server, so needs to be protected
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER))
                // Do not perform the validation
                return FormValidation.ok();
            // Validate PYTHON home
            return FormValidationUtil.validatePython(value);
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            // Trim name
            String name = Util.fixEmptyAndTrim(value);
            // Check that folder specified
            if (name == null)
                // Folder is required
                return FormValidation.error(Messages.PythonInstallation_Name_Required());
            // Check that path does not contains some whitespace chars
            if (StringUtil.hasWhitespace(name))
                // Whitespace are not allowed
                return FormValidation.error(Messages.PythonInstallation_Name_WhitespaceNotAllowed());
            // Seems fine
            return FormValidation.ok();
        }

        private static final XStream2 XSTREAM = new XStream2();

        static {
            // 0.4 to 0.5: StandardPythonInstallation becomes
            // PythonInstallation...
            XSTREAM.addCompatibilityAlias("jenkins.plugins.shiningpanda.StandardPythonInstallation",
                    PythonInstallation.class);
            // and its descriptor...
            XSTREAM.addCompatibilityAlias("jenkins.plugins.shiningpanda.StandardPythonInstallation$DescriptorImpl",
                    PythonInstallation.DescriptorImpl.class);
        }
    }
}
