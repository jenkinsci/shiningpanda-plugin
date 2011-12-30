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
package jenkins.plugins.shiningpanda.matrix;

import hudson.Extension;
import hudson.Util;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import jenkins.plugins.shiningpanda.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class ToxAxis extends Axis
{

    /**
     * Configuration name for this axis
     */
    public static final String KEY = "TOXENV";

    /**
     * Constructor using fields
     * 
     * @param values
     *            Values for this axis
     * @param extraValueString
     *            Extra values for this axis
     */
    @DataBoundConstructor
    public ToxAxis(String[] values, String extraValueString)
    {
        // Call super
        super(KEY, merge(StringUtil.fixNull(values), extraValueString));
    }

    /**
     * Constructor using fields
     * 
     * @param values
     *            Values for this axis
     */
    public ToxAxis(String[] values)
    {
        super(KEY, values);
    }

    /**
     * Merge the TOX environments
     * 
     * @param values
     *            The default values
     * @param extraValueString
     *            The custom values
     * @return The merged values
     */
    private static List<String> merge(String[] values, String extraValueString)
    {
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

    /**
     * Get the extra values as a string
     * 
     * @return The extra values as string
     */
    public String getExtraValueString()
    {
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

    /**
     * Descriptor for this axis.
     */
    @Extension
    public static class DescriptorImpl extends AxisDescriptor
    {

        /**
         * Default TOX environments
         */
        public static final List<String> DEFAULTS = Collections.unmodifiableList(Arrays.asList(new String[] { "py24", "py25",
                "py26", "py27", "py30", "py31", "py32", "jython", "pypy" }));

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName()
        {
            return Messages.ToxAxis_DisplayName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile()
        {
            return "/plugin/shiningpanda/help/matrix/ToxAxis/help.html";
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.matrix.AxisDescriptor#isInstantiable()
         */
        @Override
        public boolean isInstantiable()
        {
            // If there's no PYTHON configured, there's no point in this axis
            return !PythonInstallation.isEmpty();
        }

    }

}
