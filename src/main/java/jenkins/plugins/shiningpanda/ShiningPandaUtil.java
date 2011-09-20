package jenkins.plugins.shiningpanda;

import hudson.Util;
import hudson.model.TaskListener;
import hudson.util.FormValidation;

import java.io.File;
import java.util.regex.Pattern;

public class ShiningPandaUtil
{

    /**
     * Check that the provided value has no whitespace.
     * 
     * @param value
     *            The value to check.
     * @return True if has whitespace, else false
     */
    public static boolean hasWhitespace(String value)
    {
        if (value == null)
            return false;
        return Pattern.compile("\\s").matcher(value).find();
    }

    /**
     * Validate PYTHON name: check that specified, is not reserved and has no
     * whitespace in it.
     * 
     * @param name
     *            The PYTHON name to validate.
     * @return The validation result.
     */
    public static FormValidation validatePythonName(String name)
    {
        // Trim name
        String fixName = Util.fixEmptyAndTrim(name);
        // Check that folder specified
        if (fixName == null)
            return FormValidation.error(Messages.ShiningPandaUtil_PythonNameRequired());
        // Check that path does not contains some whitespace chars
        if (hasWhitespace(fixName))
            return FormValidation.error(Messages.ShiningPandaUtil_PythonNameHasWhitespace(fixName));
        // Check that not reserved
        if (PythonInstallation.defaultInstallationName.equals(fixName))
            return FormValidation.error(Messages
                    .ShiningPandaUtil_PythonNameReserved(PythonInstallation.defaultInstallationName));
        // Seems fine
        return FormValidation.ok();
    }

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
        if (hasWhitespace(fixHome))
            return FormValidation.error(Messages.ShiningPandaUtil_PythonHomeHasWhitespace(fixHome));
        // Seems fine
        return FormValidation.ok();
    }

    /**
     * Validate the PYTHONHOME once all variable expanded.
     * 
     * @param installation
     *            The Python installation to check.
     * @param listener
     *            The listener to display error messages.
     * @return True if validated, else false.
     */
    public static boolean validatePythonHome(PythonInstallation installation, TaskListener listener)
    {
        // Get PYTHONHOME
        String home = Util.fixEmptyAndTrim(installation.getHome());
        // Check if exists
        if (home == null)
        {
            // Log
            listener.fatalError(Messages.ShiningPandaUtil_PythonHomeRequired());
            // Invalid PYTHONHOME
            return false;
        }
        // Check if has whitespace in it
        if (hasWhitespace(home))
        {
            // Log
            listener.fatalError(Messages.ShiningPandaUtil_PythonHomeHasWhitespace(home));
            // Invalid PYTHONHOME
            return false;
        }
        // Seems fine
        return true;
    }

}
