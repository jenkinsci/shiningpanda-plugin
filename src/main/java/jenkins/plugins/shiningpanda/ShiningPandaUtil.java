package jenkins.plugins.shiningpanda;

import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
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
     *            The PYTHON installation to check.
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

    /**
     * Validate the PYTHONHOME once all variable expanded.
     * 
     * @param installation
     *            The PYTHON installation to check.
     * @param launcher
     *            The launcher.
     * @param listener
     *            The listener to display error messages.
     * @return True if validated, else false.
     * @throws InterruptedException
     * @throws IOException
     */
    public static boolean validatePythonInstallation(PythonInstallation installation, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException
    {
        // Check if installation exists
        if (installation == null)
        {
            // Log
            listener.fatalError(Messages.ShiningPandaUtil_PythonInstallationNotFound());
            // No installation configured in JENKINS management page
            return false;
        }
        // Check PYTHONHOME
        if (!validatePythonHome(installation, listener))
            // Fail to validate PYTHON installation
            return false;
        // Get the executable
        String exe = installation.getExecutable(launcher);
        // Check if executable exists
        if (exe == null)
        {
            // Log
            listener.fatalError(Messages.ShiningPandaUtil_PythonExeNotFound(installation.getHome()));
            // Executable not found
            return false;
        }
        // Seems fine
        return true;
    }

    /**
     * Get the path separator of the node where the build runs
     * 
     * @param launcher
     *            The task launcher
     * @return The remote path separator
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("serial")
    public static String getPathSeparator(Launcher launcher) throws IOException, InterruptedException
    {
        return launcher.getChannel().call(new Callable<String, IOException>()
        {
            public String call() throws IOException
            {
                return File.pathSeparator;
            }
        });
    }
}
