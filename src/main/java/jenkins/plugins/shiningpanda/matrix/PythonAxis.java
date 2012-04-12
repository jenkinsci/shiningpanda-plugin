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
import hudson.Functions;
import hudson.Util;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;

import java.util.Arrays;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.ShiningPanda;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

import org.kohsuke.stapler.DataBoundConstructor;

public class PythonAxis extends Axis
{

    /**
     * Configuration name for this axis
     */
    public static final String KEY = "PYTHON";

    /**
     * Constructor using fields
     * 
     * @param values
     *            Values for this axis
     */
    @DataBoundConstructor
    public PythonAxis(String[] values)
    {
        super(KEY, Arrays.asList(values));
    }

    /**
     * Get this axis values for tree handling.
     * 
     * @return The joined values
     */
    public String getTreeValueString()
    {
        return Util.join(getValues(), "/");
    }

    /**
     * Descriptor for this axis.
     */
    @Extension
    public static class DescriptorImpl extends AxisDescriptor
    {

        /**
         * Let Jelly access the hosted flag.
         */
        public static boolean HOSTED = ShiningPanda.HOSTED;

        /**
         * Store a tree helper.
         */
        public static PythonAxisTree TREE = new PythonAxisTree();

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName()
        {
            return Messages.PythonAxis_DisplayName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile()
        {
            return Functions.getResourcePath() + "/plugin/shiningpanda/help/matrix/PythonAxis/help.html";
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

        /**
         * Get the list of PYTHON installations.
         * 
         * @return The list of installations
         */
        public PythonInstallation[] getInstallations()
        {
            // Delegate
            return PythonInstallation.list();
        }

        /**
         * Escape strings
         * 
         * @param body
         *            The body
         * @param args
         *            The arguments
         * @return The escaped string
         */
        public String jsStringEscape(String body, Object... args)
        {
            return '\"' + Functions.jsStringEscape(String.format(body, args)) + '\"';
        }

        /**
         * Escape strings
         * 
         * @param body
         *            The body
         * @return The escaped string
         */
        public String jsStringEscape(String body)
        {
            return jsStringEscape(body, new Object[] {});
        }

        /**
         * Build the tree check box
         * 
         * @param installation
         *            The PYTHON installation
         * @return The HTML
         */
        public String buildCheckBox(PythonInstallation installation)
        {
            return jsStringEscape("<input type='checkbox' name='values' json='%s' ",
                    Functions.htmlAttributeEscape(installation.getName()))
                    + String.format("+has(%s)+", jsStringEscape(installation.getName()))
                    + jsStringEscape("/><label class='attach-previous'>%s</label>", TREE.getVersion(installation));
        }
    }
}
