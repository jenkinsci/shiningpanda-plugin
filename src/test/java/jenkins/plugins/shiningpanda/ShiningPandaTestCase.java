package jenkins.plugins.shiningpanda;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jvnet.hudson.test.HudsonTestCase;

public abstract class ShiningPandaTestCase extends HudsonTestCase
{

    /**
     * Key for CPython 2.x's home in test.properties file.
     */
    private final static String CPYTHON_2_HOME_KEY = "CPython.2.Home";

    /**
     * Key for CPython 3.x's home in test.properties file.
     */
    private final static String CPYTHON_3_HOME_KEY = "CPython.3.Home";

    /**
     * Key for PyPy's home in test.properties file.
     */
    private final static String PYPY_HOME_KEY = "PyPy.Home";

    /**
     * Load the test properties. First load the test.properties.model file, and
     * then test.properties if exists.
     * 
     * @return The test properties.
     * @throws IOException
     */
    protected Properties getTestProperties() throws IOException
    {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/test.properties.model"));
        InputStream stream = getClass().getResourceAsStream("/test.properties");
        if (stream != null)
            properties.load(stream);
        return properties;
    }

    /**
     * Get the value for the given test properties key.
     * 
     * @param key
     *            The key.
     * @return The value.
     * @throws IOException
     */
    protected String getTestProperty(String key) throws IOException
    {
        String value = System.getProperty(key);
        if (value == null)
            value = getTestProperties().getProperty(key);
        if (value == null)
            throw new IOException("failed to find a value for " + key);
        return value;
    }

    /**
     * Get the CPython 2.x's home.
     * 
     * @return The home folder.
     * @throws IOException
     */
    protected String getCPython2Home() throws IOException
    {
        return getTestProperty(CPYTHON_2_HOME_KEY);
    }

    /**
     * Get the PyPy's home.
     * 
     * @return The home folder.
     * @throws IOException
     */
    protected String getPyPyHome() throws IOException
    {
        return getTestProperty(PYPY_HOME_KEY);
    }

    /**
     * Get the CPython 3.x's home
     * 
     * @return The home folder.
     * @throws IOException
     */
    protected String getCPython3Home() throws IOException
    {
        return getTestProperty(CPYTHON_3_HOME_KEY);
    }

    /**
     * Configure a Python installation.
     * 
     * @param name
     *            The name of the installation.
     * @param home
     *            The home folder for this installation.
     * @return
     */
    protected StandardPythonInstallation configurePython(String name, String home)
    {
        StandardPythonInstallation[] installations = getPythonInstallations();
        StandardPythonInstallation[] newIntallations = new StandardPythonInstallation[installations.length + 1];
        int index = 0;
        for (StandardPythonInstallation installation : installations)
        {
            newIntallations[index] = installation;
            index++;
        }
        StandardPythonInstallation newInstallation = new StandardPythonInstallation(name, home, NO_PROPERTIES);
        newIntallations[index] = newInstallation;
        getPythonInstallationDescriptor().setInstallations(newIntallations);
        return newInstallation;
    }

    /**
     * Configure a CPython 2.x installation.
     * 
     * @return The installation.
     * @throws Exception
     */
    protected StandardPythonInstallation configureCPython2() throws Exception
    {
        return configurePython("CPython-2", getCPython2Home());
    }

    /**
     * Configure a CPython 3.x installation.
     * 
     * @return The installation.
     * @throws Exception
     */
    protected StandardPythonInstallation configureCPython3() throws Exception
    {
        return configurePython("CPython-3", getCPython3Home());
    }

    /**
     * Configure a PyPy installation.
     * 
     * @return The installation.
     * @throws Exception
     */
    protected StandardPythonInstallation configurePyPy() throws Exception
    {
        return configurePython("PyPy", getPyPyHome());
    }

    /**
     * Get the Python's installations descriptor.
     * 
     * @return The descriptor.
     */
    protected StandardPythonInstallation.DescriptorImpl getPythonInstallationDescriptor()
    {
        return hudson.getDescriptorByType(StandardPythonInstallation.DescriptorImpl.class);
    }

    /**
     * Get the list of Python's installations.
     * 
     * @return The list of installations.
     */
    protected StandardPythonInstallation[] getPythonInstallations()
    {
        return getPythonInstallationDescriptor().getInstallations();
    }

}
