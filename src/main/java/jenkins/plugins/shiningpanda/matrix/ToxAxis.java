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
import hudson.Util;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import jenkins.plugins.shiningpanda.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ToxAxis extends Axis {
    public static final String KEY = "TOXENV";

    @DataBoundConstructor
    public ToxAxis(String[] values, String extraValueString) {
        // Call super
        super(KEY, merge(StringUtil.fixNull(values), extraValueString));
    }

    public ToxAxis(String[] values) {
        super(KEY, values);
    }

    private static List<String> merge(String[] values, String extraValueString) {
        // Merged values
        List<String> allValues = new ArrayList<String>();
        // Go threw the default values
        for (String value : Arrays.asList(values))
            // Check if already contained
            if (!allValues.contains(value))
                // If not add it
                allValues.add(value);
        // Parse the extra values
        for (String value : Util.tokenize(extraValueString))
            // Check if already contained
            if (!allValues.contains(value))
                // If not add it
                allValues.add(value);
        // Return the merged list
        return allValues;
    }

    public String getExtraValueString() {
        // Get the extra values as a string
        List<String> extraValues = new ArrayList<String>();
        // Go threw the values
        for (String value : getValues())
            // Check if in default values
            if (!DescriptorImpl.DEFAULTS.contains(value))
                // If not add it
                extraValues.add(value);
        // Join the extra value list to display them as char field
        return StringUtils.join(extraValues, " ");
    }

    @Extension
    public static class DescriptorImpl extends AxisDescriptor {
        public static final List<String> DEFAULTS = Collections.unmodifiableList(Arrays.asList(
                new String[]{"py26", "py27", "py32", "py33", "py34", "py35", "pypy", "jython"}));

        @Override
        public String getDisplayName() {
            return Messages.ToxAxis_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return Functions.getResourcePath() + "/plugin/shiningpanda/help/matrix/ToxAxis/help.html";
        }

        @Override
        public boolean isInstantiable() {
            // If there's no PYTHON configured, there's no point in this axis
            return !PythonInstallation.isEmpty();
        }

    }

}
