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
package jenkins.plugins.shiningpanda.util;

import java.io.File;

import hudson.Util;
import hudson.util.FormValidation;
import jenkins.plugins.shiningpanda.Messages;

public class FormValidationUtil
{

    /**
     * Validate PYTHON home: check that specified, exists and has no whitespace
     * in it.
     * 
     * @param home
     *            The PYTHON home to validate.
     * @return The validation result.
     */
    public static FormValidation validatePythonHome(File home)
    {
        // Trim home
        String fixHome = Util.fixEmptyAndTrim(home.getPath());
        // Check that folder specified
        if (fixHome == null)
            return FormValidation.error(Messages.ShiningPandaUtil_PythonHomeRequired());
        // Check that folder exists. If not exists, just display a warning
        // as installation can be on slaves
        if (!home.isDirectory())
            return FormValidation.warning(Messages.ShiningPandaUtil_PythonHomeNotADirectory(fixHome));
        // Check that path does not contains some whitespace chars
        if (StringUtil.hasWhitespace(fixHome))
            return FormValidation.error(Messages.ShiningPandaUtil_PythonHomeHasWhitespace(fixHome));
        // Seems fine
        return FormValidation.ok();
    }

}
