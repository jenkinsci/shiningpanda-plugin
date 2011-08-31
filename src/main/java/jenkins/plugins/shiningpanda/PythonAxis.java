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
package jenkins.plugins.shiningpanda;

import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.tools.ToolInstallation;

import java.util.Arrays;

import org.kohsuke.stapler.DataBoundConstructor;

public class PythonAxis extends Axis
{

    /**
     * Configuration name for this axis
     */
    public static final String KEY = "python";

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
     * Descriptor for this axis.
     */
    @Extension
    public static class DescriptorImpl extends AxisDescriptor
    {

        /**
         * Let the key available in Jelly
         */
        public static final String pythonVar = KEY;

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
            return "/plugin/shiningpanda/help/PythonAxis/help.html";
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
            return getInstallations().length != 0;
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
