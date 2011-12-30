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
package jenkins.plugins.shiningpanda;

import hudson.FilePath;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixProject;
import hudson.model.Item;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Properties;

import jenkins.plugins.shiningpanda.matrix.PythonAxis;
import jenkins.plugins.shiningpanda.matrix.ToxAxis;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import jenkins.plugins.shiningpanda.workspace.MasterWorkspace;
import jenkins.plugins.shiningpanda.workspace.SlaveWorkspace;
import jenkins.plugins.shiningpanda.workspace.Workspace;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
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
     * Key for Jython's home in test.properties file.
     */
    private final static String JYTHON_HOME_KEY = "Jython.Home";

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
     * Name of JYTHON.
     */
    private final static String JYTHON_NAME = "Jython";

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
     * Get the Jython's home.
     * 
     * @return The home folder.
     * @throws IOException
     */
    protected String getJythonHome() throws IOException
    {
        return getTestProperty(JYTHON_HOME_KEY);
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
     * Delete a VIRTUALENV.
     * 
     * @param home
     *            The home folder of the VIRTUALENV.
     * @throws IOException
     */
    protected void deleteVirtualenv(File home) throws IOException
    {
        // Check if exists
        if (!home.exists())
            return;
        // Do not follow symbolic links
        IOFileFilter filter = new IOFileFilter()
        {
            /*
             * (non-Javadoc)
             * 
             * @see
             * org.apache.commons.io.filefilter.IOFileFilter#accept(java.io.
             * File, java.lang.String)
             */
            public boolean accept(File dir, String name)
            {
                return accept(dir);
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.apache.commons.io.filefilter.IOFileFilter#accept(java.io.
             * File)
             */
            public boolean accept(File file)
            {
                try
                {
                    return !FileUtils.isSymlink(file);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                return false;
            }
        };
        // Go threw the selected files to set write permission
        for (File file : FileUtils.listFiles(home, filter, filter))
        {
            // Set write permission
            file.setWritable(true);
        }
        // Delete the directory
        FileUtils.deleteDirectory(home);
    }

    /**
     * Create a VIRTUALENV.
     * 
     * @param home
     *            The home of this VIRTUALENV.
     * @return The home of the VIRTUALENV
     * @throws Exception
     */
    protected File createVirtualenv(File home) throws Exception
    {
        // Clean
        deleteVirtualenv(home);
        // Create a process to create the VIRTUALENV
        ProcessBuilder pb = new ProcessBuilder("virtualenv", home.getAbsolutePath());
        // Start the process
        Process process = pb.start();
        // Check exit code
        assertEquals(0, process.waitFor());
        // Return the home folder
        return home;
    }

    /**
     * Create a fake PYTHON installation with spaces in its home folder path.
     * 
     * @return The home folder
     * @throws IOException
     */
    protected File createFakePythonInstallationWithWhitespaces() throws IOException
    {
        // Create a home folder with spaces in its name
        File home = createTmpDir("bad move");
        // Cleanup if already exists
        FileUtils.deleteDirectory(home);
        // Get the binary folder
        File bin = new File(home, "bin");
        // Create it
        bin.mkdir();
        // Get the PYTHON binary path
        File binary = new File(bin, "python");
        // Create the file
        FileUtils.writeStringToFile(binary, "fake installation");
        // Return home folder
        return home;
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
    protected PythonInstallation configurePython(String name, String home)
    {
        PythonInstallation[] installations = getPythonInstallations();
        PythonInstallation[] newIntallations = new PythonInstallation[installations.length + 1];
        int index = 0;
        for (PythonInstallation installation : installations)
        {
            newIntallations[index] = installation;
            index++;
        }
        PythonInstallation newInstallation = new PythonInstallation(name, home, NO_PROPERTIES);
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
    protected PythonInstallation configureCPython2() throws Exception
    {
        return configurePython(CPYTHON_2_NAME, getCPython2Home());
    }

    /**
     * Configure a CPython 3.x installation.
     * 
     * @return The installation.
     * @throws Exception
     */
    protected PythonInstallation configureCPython3() throws Exception
    {
        return configurePython(CPYTHON_3_NAME, getCPython3Home());
    }

    /**
     * Configure a PyPy installation.
     * 
     * @return The installation.
     * @throws Exception
     */
    protected PythonInstallation configurePyPy() throws Exception
    {
        return configurePython(PYPY_NAME, getPyPyHome());
    }

    /**
     * Configure a JYTHON installation.
     * 
     * @return The installation.
     * @throws Exception
     */
    protected PythonInstallation configureJython() throws Exception
    {
        return configurePython(JYTHON_NAME, getJythonHome());
    }

    /**
     * Configure all Python installations.
     * 
     * @return List of Python installations.
     * @throws Exception
     */
    protected PythonInstallation[] configureAllPythons() throws Exception
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
    protected PythonInstallation.DescriptorImpl getPythonInstallationDescriptor()
    {
        return hudson.getDescriptorByType(PythonInstallation.DescriptorImpl.class);
    }

    /**
     * Get the list of Python's installations.
     * 
     * @return The list of installations.
     */
    protected PythonInstallation[] getPythonInstallations()
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
     * Performs a configuration round-trip testing for a builder on a matrix
     * project with a Python axis.
     * 
     * @param before
     *            The builder.
     * @return The reloaded builder.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected <B extends Builder> B configPythonMatrixRoundtrip(B before) throws Exception
    {
        configureAllPythons();
        MatrixProject p = createMatrixProject();
        p.setAxes(new AxisList(new PythonAxis(new String[] { CPYTHON_2_NAME, CPYTHON_3_NAME, PYPY_NAME, JYTHON_NAME })));
        p.getBuildersList().add(before);
        configRoundtrip((Item) p);
        return (B) p.getBuildersList().get(before.getClass());
    }

    /**
     * Performs a configuration round-trip testing for a builder on a matrix
     * project with a Tox axis.
     * 
     * @param before
     *            The builder.
     * @return The reloaded builder.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected <B extends Builder> B configToxMatrixRoundtrip(B before) throws Exception
    {
        configureAllPythons();
        MatrixProject p = createMatrixProject();
        p.setAxes(new AxisList(new ToxAxis(new String[] { "py27", "py32", "pypy", "jython" })));
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

    /**
     * Get the JENKINS configuration file.
     * 
     * @return The configuration file.
     */
    public File getConfigFile()
    {
        return new File(jenkins.getRootDir(), "config.xml");
    }

    /**
     * Get the configuration file for this project.
     * 
     * @param project
     *            The project
     * @return The configuration file
     */
    public File getConfigFile(AbstractProject<?, ?> project)
    {
        return new File(jenkins.getRootDir(), StringUtils.join(new String[] { "jobs", project.getName(), "config.xml" },
                File.separator));
    }

    /**
     * Create a workspace.
     * 
     * @return The workspace
     * @throws IOException
     */
    public Workspace getWorkspace() throws IOException
    {
        return Workspace.fromHome(new FilePath(createTmpDir()));
    }

    /**
     * Create a master workspace.
     * 
     * @return The workspace
     * @throws IOException
     */
    public MasterWorkspace getMasterWorkspace() throws IOException
    {
        return new MasterWorkspace(new FilePath(createTmpDir()));
    }

    /**
     * Create a slave workspace.
     * 
     * @return The workspace
     * @throws IOException
     */
    public SlaveWorkspace getSlaveWorkspace() throws IOException
    {
        return new SlaveWorkspace(new FilePath(createTmpDir()));
    }

    /**
     * Get the packages directory.
     * 
     * @return The package directory
     */
    public File getPackagesDir()
    {
        return new File(jenkins.getRootDir(), "shiningpanda" + File.separator + "packages");
    }

    /**
     * Create the packages directory.
     * 
     * @return The packages directory
     */
    public File createPackagesDir()
    {
        File packagesDir = getPackagesDir();
        packagesDir.mkdirs();
        return packagesDir;
    }

    public File createTmpDir(String... parts) throws IOException
    {
        File file = new File(createTmpDir(), StringUtils.join(parts, File.separator));
        file.mkdirs();
        return file;
    }

    /**
     * Convert a FilePath to a File.
     * 
     * @param filePath
     *            The FilePath to convert
     * @return The resulting file
     */
    public File toFile(FilePath filePath)
    {
        return new File(filePath.getRemote());
    }

    /**
     * Assert that file exists
     * 
     * @param file
     *            The file to check
     */
    public void assertFile(File file)
    {
        assertTrue("file does not exist: " + file.getAbsolutePath(), file.isFile());
    }

    /**
     * Assert that file exists
     * 
     * @param filePath
     *            The file to check
     */
    public void assertFile(FilePath filePath)
    {
        assertFile(toFile(filePath));
    }

    /**
     * Assert that directory exists
     * 
     * @param file
     *            The directory to check
     */
    public void assertDirectory(File file)
    {
        assertTrue("directory does not exist: " + file.getAbsolutePath(), file.isDirectory());
    }

    /**
     * Assert that directory exists
     * 
     * @param filePath
     *            The directory to check
     */
    public void assertDirectory(FilePath filePath)
    {
        assertDirectory(toFile(filePath));
    }

    /**
     * Assert that file does not exist
     * 
     * @param file
     *            The file to check
     */
    public void assertNotExists(File file)
    {
        assertFalse("file exists: " + file.getAbsolutePath(), file.exists());
    }

    /**
     * Assert that file does not exist
     * 
     * @param filePath
     *            The file to check
     */
    public void assertNotExists(FilePath filePath)
    {
        assertNotExists(toFile(filePath));
    }

    /**
     * Check that files contains the same thing.
     * 
     * @param file1
     *            The first file
     * @param file2
     *            The second file
     */
    public void assertContentEquals(File file1, File file2)
    {
        assertFile(file1);
        assertFile(file2);
        try
        {
            assertEquals("file content differ: " + file1.getAbsolutePath() + " != " + file2.getAbsolutePath(),
                    FileUtils.readFileToString(file1), FileUtils.readFileToString(file2));
        }
        catch (IOException e)
        {
            fail("failed to read file content: " + e.getMessage());
        }
    }

    /**
     * Check that files contains the same thing.
     * 
     * @param file1
     *            The first file
     * @param file2
     *            The second file
     */
    public void assertContentEquals(FilePath filePath1, FilePath filePath2)
    {
        assertContentEquals(toFile(filePath1), toFile(filePath2));
    }

    /**
     * Get a FilePath given a path as string
     * 
     * @param pathname
     *            The path
     * @return The file object
     */
    public FilePath getFilePath(String pathname)
    {
        return new FilePath(new File(pathname));
    }
}
