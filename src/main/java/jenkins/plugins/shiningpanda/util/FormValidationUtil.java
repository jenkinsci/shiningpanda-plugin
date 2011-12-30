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
package jenkins.plugins.shiningpanda.util;

import hudson.Util;
import hudson.util.FormValidation;

import java.io.File;

import jenkins.plugins.shiningpanda.Messages;

public class FormValidationUtil
{

    /**
     * Validate PYTHON home: verify that specified, exists and has no whitespace
     * in it.
     * 
     * @param home
     *            The PYTHON home to validate.
     * @return The validation result.
     */
    public static FormValidation validatePythonHome(String home)
    {
        // Get the file value as a string
        home = Util.fixEmptyAndTrim(home);
        // Check is a value was provided
        if (home == null)
            // Value is required
            return FormValidation.error(Messages.FormValidationUtil_PythonHome_Required());
        // Expand the home
        home = EnvVarsUtil.expand(home);
        // Check that path does not contains some whitespace chars
        if (StringUtil.hasWhitespace(home))
            // No whitespace allowed
            return FormValidation.error(Messages.FormValidationUtil_PythonHome_WhitespaceNotAllowed());
        // Get a file
        File file = new File(home);
        // Check if absolute
        if (!file.isAbsolute())
            // Absolute path required
            return FormValidation.error(Messages.FormValidationUtil_PythonHome_AbsolutePathRequired());
        // Check that folder exists
        if (!file.isDirectory())
            // Display a warning
            return FormValidation.error(Messages.FormValidationUtil_PythonHome_NotADirectory());
        // Seems fine
        return FormValidation.ok();
    }

}
