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
        String sHome = Util.fixEmptyAndTrim(home.getPath());
        // Check that folder specified
        if (sHome == null)
            return FormValidation.error(Messages.ShiningPandaUtil_PythonHomeRequired_Short());
        // Check that folder exists. If not exists, just display a warning
        // as installation can be on slaves
        if (!home.isDirectory())
            return FormValidation.warning(Messages.ShiningPandaUtil_PythonHomeNotADirectory(sHome));
        // Check that path does not contains some whitespace chars
        if (hasWhitespace(sHome))
            return FormValidation.error(Messages.ShiningPandaUtil_PythonHomeHasWhitespace_Short(sHome));
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
            listener.fatalError(Messages.ShiningPandaUtil_PythonHomeRequired_Short());
            // Invalid PYTHONHOME
            return false;
        }
        // Check if has whitespace in it
        if (hasWhitespace(home))
        {
            // Log
            listener.fatalError(Messages.ShiningPandaUtil_PythonHomeHasWhitespace_Long(home));
            // Invalid PYTHONHOME
            return false;
        }
        // Seems fine
        return true;
    }

}
