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
package jenkins.plugins.shiningpanda.matrix;

import hudson.Extension;
import hudson.Functions;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Arrays;

public class PythonAxis extends Axis {
    public static final String KEY = "PYTHON";

    @DataBoundConstructor
    public PythonAxis(String[] values) {
        super(KEY, Arrays.asList(values));
    }

    public String getTreeValueString() {
        return String.join("/", getValues());
    }

    @Extension
    public static class DescriptorImpl extends AxisDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.PythonAxis_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return Functions.getResourcePath() + "/plugin/shiningpanda/help/matrix/PythonAxis/help.html";
        }

        @Override
        public boolean isInstantiable() {
            // If there's no PYTHON configured, there's no point in this axis
            return !PythonInstallation.isEmpty();
        }

        public PythonInstallation[] getInstallations() {
            // Delegate
            return PythonInstallation.list();
        }
    }
}
