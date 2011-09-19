package jenkins.plugins.shiningpanda;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixProject;
import hudson.model.Item;
import hudson.tasks.Builder;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
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
     * Name of CPython 2.x.
     */
    private final static String CPYTHON_2_NAME = "CPython-2";

    /**
     * Name of CPython 3.x.
     */
    private final static String CPYTHON_3_NAME = "CPython-3";

    /**
     * Name of PyPy.
     */
    private final static String PYPY_NAME = "PyPy";

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
     * Get the Virtualenv's home
     * 
     * @return The home folder.
     * @throws IOException
     */
    protected String getVirtualenvHome() throws IOException
    {
        return new File("target", "virtualenv").getAbsolutePath();
    }

    /**
     * Create a VIRTUALENV.
     * 
     * @param home
     *            The home of this VIRTUALENV.
     * @return The home of the VIRTUALENV
     * @throws Exception
     */
    protected String createVirtualenv(String home) throws Exception
    {
        File file = new File(home);
        if (file.isDirectory())
            FileUtils.deleteDirectory(file);
        if (file.isFile())
            file.delete();
        ProcessBuilder pb = new ProcessBuilder("virtualenv", home);
        Process process = pb.start();
        assertEquals(0, process.waitFor());
        return home;
    }

    /**
     * Create a default VIRTUALENV.
     * 
     * @return The home of this VIRTUALENV
     * @throws Exception
     */
    protected String createVirtualenv() throws Exception
    {
        return createVirtualenv(getVirtualenvHome());
    }

    /**
     * Configure a PYTHON installation.
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
        return configurePython(CPYTHON_2_NAME, getCPython2Home());
    }

    /**
     * Configure a CPython 3.x installation.
     * 
     * @return The installation.
     * @throws Exception
     */
    protected StandardPythonInstallation configureCPython3() throws Exception
    {
        return configurePython(CPYTHON_3_NAME, getCPython3Home());
    }

    /**
     * Configure a PyPy installation.
     * 
     * @return The installation.
     * @throws Exception
     */
    protected StandardPythonInstallation configurePyPy() throws Exception
    {
        return configurePython(PYPY_NAME, getPyPyHome());
    }

    /**
     * Configure all Python installations.
     * 
     * @return List of Python installations.
     * @throws Exception
     */
    protected StandardPythonInstallation[] configureAllPythons() throws Exception
    {
        configureCPython2();
        configureCPython3();
        configurePyPy();
        return getPythonInstallations();
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

    /**
     * Performs a configuration round-trip testing for a builder on free-style
     * project.
     * 
     * @param before
     *            The builder.
     * @return The reloaded builder.
     * @throws Exception
     */
    protected <B extends Builder> B configFreeStyleRoundtrip(B before) throws Exception
    {
        return configRoundtrip(before);
    }

    /**
     * Performs a configuration round-trip testing for a builder on free-style
     * project.
     * 
     * @param before
     *            The builder.
     * @return The reloaded builder.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected <B extends Builder> B configMatrixRoundtrip(B before) throws Exception
    {
        configureAllPythons();
        MatrixProject p = createMatrixProject();
        p.setAxes(new AxisList(new PythonAxis(new String[] { CPYTHON_2_NAME, CPYTHON_3_NAME, PYPY_NAME })));
        p.getBuildersList().add(before);
        configRoundtrip((Item) p);
        return (B) p.getBuildersList().get(before.getClass());
    }

    /**
     * Search a field in the provided class and its super classes.
     * 
     * @param klass
     *            The class to search in.
     * @param p
     *            The field to search.
     * @return The field, or null if no such field.
     */
    protected Field getField(@SuppressWarnings("rawtypes") Class klass, String p)
    {
        while (klass != Object.class)
        {
            try
            {
                return klass.getDeclaredField(p);
            }
            catch (NoSuchFieldException e)
            {
            }
            klass = klass.getSuperclass();
        }
        return null;
    }

    /**
     * Same as assertEqualBeans, but works on protected and private fields.
     * 
     * @param lhs
     *            The initial object.
     * @param rhs
     *            The final object.
     * @param properties
     *            The properties to check.
     * @throws Exception
     */
    public void assertEqualBeans2(Object lhs, Object rhs, String properties) throws Exception
    {
        assertNotNull("lhs is null", lhs);
        assertNotNull("rhs is null", rhs);
        for (String p : properties.split(","))
        {
            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(lhs, p);
            Object lp, rp;
            if (pd == null)
            {
                Field f = getField(lhs.getClass(), p);
                assertNotNull("No such property " + p + " on " + lhs.getClass(), f);
                boolean accessible = f.isAccessible();
                if (!accessible)
                    f.setAccessible(true);
                lp = f.get(lhs);
                rp = f.get(rhs);
                f.setAccessible(accessible);
            }
            else
            {
                lp = PropertyUtils.getProperty(lhs, p);
                rp = PropertyUtils.getProperty(rhs, p);
            }

            if (lp != null && rp != null && lp.getClass().isArray() && rp.getClass().isArray())
            {
                // deep array equality comparison
                int m = Array.getLength(lp);
                int n = Array.getLength(rp);
                assertEquals("Array length is different for property " + p, m, n);
                for (int i = 0; i < m; i++)
                    assertEquals(p + "[" + i + "] is different", Array.get(lp, i), Array.get(rp, i));
                return;
            }

            assertEquals("Property " + p + " is different", lp, rp);
        }
    }
}
