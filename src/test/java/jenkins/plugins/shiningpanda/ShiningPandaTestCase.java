/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2015 ShiningPanda S.A.S.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of its license which incorporates the terms and
 * conditions of version 3 of the GNU Affero General Public License,
 * supplemented by the additional permissions under the GNU Affero GPL
 * version 3 section 7: if you modify this program, or any covered work,
 * by linking or combining it with other code, such other code is not
 * for that reason alone subject to any of the requirements of the GNU
 * Affero GPL version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * license for more details.
 *
 * You should have received a copy of the license along with this program.
 * If not, see <https://raw.github.com/jenkinsci/shiningpanda-plugin/master/LICENSE.txt>.
 */
package jenkins.plugins.shiningpanda;

import hudson.FilePath;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.tasks.Builder;
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
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.jvnet.hudson.test.JenkinsRule.NO_PROPERTIES;

public abstract class ShiningPandaTestCase {

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

    @Rule
    public JenkinsRule j = new JenkinsRule();
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Load the test properties. First load the test.properties.model file, and
     * then test.properties if exists.
     *
     * @return The test properties.
     */
    protected Properties getTestProperties() {

        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream("/test.properties.model"));
            InputStream stream = getClass().getResourceAsStream("/test.properties");
            if (stream != null)
                properties.load(stream);
            return properties;
        } catch (IOException e) {
            throw new ShiningPandaTestException(e);
        }
    }

    /**
     * Get the value for the given test properties key.
     *
     * @param key The key.
     * @return The value.
     */
    protected String getTestProperty(String key) {
        String value = System.getProperty(key);
        if (value == null)
            value = getTestProperties().getProperty(key);
        if (value == null)
            throw new IllegalArgumentException("failed to find a value for " + key);
        return value;
    }

    /**
     * Get the CPython 2.x's home.
     *
     * @return The home folder.
     */
    protected String getCPython2Home() {
        return getTestProperty(CPYTHON_2_HOME_KEY);
    }

    /**
     * Get the PyPy's home.
     *
     * @return The home folder.
     */
    protected String getPyPyHome() {
        return getTestProperty(PYPY_HOME_KEY);
    }

    /**
     * Get the Jython's home.
     *
     * @return The home folder.
     */
    protected String getJythonHome() {
        return getTestProperty(JYTHON_HOME_KEY);
    }

    /**
     * Get the CPython 3.x's home
     *
     * @return The home folder.
     */
    protected String getCPython3Home() {
        return getTestProperty(CPYTHON_3_HOME_KEY);
    }

    /**
     * Delete a VIRTUALENV.
     *
     * @param home The home folder of the VIRTUALENV.
     */
    protected void deleteVirtualenv(File home) {
        // Check if exists
        if (!home.exists())
            return;
        // Do not follow symbolic links
        IOFileFilter filter = new IOFileFilter() {
            /*
             * (non-Javadoc)
             *
             * @see
             * org.apache.commons.io.filefilter.IOFileFilter#accept(java.io.
             * File, java.lang.String)
             */
            public boolean accept(File dir, String name) {
                return accept(dir);
            }

            /*
             * (non-Javadoc)
             *
             * @see
             * org.apache.commons.io.filefilter.IOFileFilter#accept(java.io.
             * File)
             */
            public boolean accept(File file) {
                return !FileUtils.isSymlink(file);
            }
        };
        // Go threw the selected files to set write permission
        for (File file : FileUtils.listFiles(home, filter, filter)) {
            // Set write permission
            file.setWritable(true);
        }
        // Delete the directory
        try {
            FileUtils.deleteDirectory(home);
        } catch (IOException e) {
            throw new ShiningPandaTestException(e);
        }
    }

    /**
     * Create a VIRTUALENV.
     *
     * @param home The home of this VIRTUALENV.
     * @return The home of the VIRTUALENV
     */
    protected File createVirtualenv(File home) {
        // Clean
        deleteVirtualenv(home);
        // Create a process to create the VIRTUALENV
        ProcessBuilder pb = new ProcessBuilder("virtualenv", home.getAbsolutePath());
        try {
            // Start the process
            Process process = pb.start();
            // Check exit code
            assertEquals(0, process.waitFor());
            // Return the home folder
            return home;
        } catch (IOException | InterruptedException e) {
            throw new ShiningPandaTestException(e);
        }
    }

    /**
     * Create a fake PYTHON installation with spaces in its home folder path.
     *
     * @return The home folder
     */
    protected File createFakePythonInstallationWithWhitespaces() {
        try {
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
            FileUtils.writeStringToFile(binary, "fake installation", StandardCharsets.UTF_8);
            // Return home folder
            return home;
        } catch (IOException e) {
            throw new ShiningPandaTestException(e);
        }
    }

    /**
     * Configure a PYTHON installation.
     *
     * @param name The name of the installation.
     * @param home The home folder for this installation.
     * @return the new python installation
     */
    protected PythonInstallation configurePython(String name, String home) {
        PythonInstallation[] installations = getPythonInstallations();
        PythonInstallation[] newIntallations = new PythonInstallation[installations.length + 1];
        int index = 0;
        for (PythonInstallation installation : installations) {
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
     */
    protected PythonInstallation configureCPython2() {
        return configurePython(CPYTHON_2_NAME, getCPython2Home());
    }

    /**
     * Configure a CPython 3.x installation.
     *
     * @return The installation.
     */
    protected PythonInstallation configureCPython3() {
        return configurePython(CPYTHON_3_NAME, getCPython3Home());
    }

    /**
     * Configure a PyPy installation.
     *
     * @return The installation.
     */
    protected PythonInstallation configurePyPy() {
        return configurePython(PYPY_NAME, getPyPyHome());
    }

    /**
     * Configure a JYTHON installation.
     *
     * @return The installation.
     */
    protected PythonInstallation configureJython() {
        return configurePython(JYTHON_NAME, getJythonHome());
    }

    /**
     * Configure all Python installations.
     *
     * @return List of Python installations.
     */
    protected PythonInstallation[] configureAllPythons() {
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
    protected PythonInstallation.DescriptorImpl getPythonInstallationDescriptor() {
        return j.get(PythonInstallation.DescriptorImpl.class);
    }

    /**
     * Get the list of Python's installations.
     *
     * @return The list of installations.
     */
    protected PythonInstallation[] getPythonInstallations() {
        return getPythonInstallationDescriptor().getInstallations();
    }

    /**
     * Performs a configuration round-trip testing for a builder on free-style
     * project.
     *
     * @param before The builder.
     * @return The reloaded builder.
     * @throws Exception
     */
    protected <B extends Builder> B configFreeStyleRoundtrip(B before) throws Exception {
        return j.configRoundtrip(before);
    }

    /**
     * Performs a configuration round-trip testing for a builder on a matrix
     * project with a Python axis.
     *
     * @param before The builder.
     * @return The reloaded builder.
     */
    @SuppressWarnings("unchecked")
    protected <B extends Builder> B configPythonMatrixRoundtrip(B before) {
        configureAllPythons();
        try {
            MatrixProject p = j.createProject(MatrixProject.class);
            p.setAxes(
                    new AxisList(new PythonAxis(new String[]{CPYTHON_2_NAME, CPYTHON_3_NAME, PYPY_NAME, JYTHON_NAME})));
            p.getBuildersList().add(before);
            j.configRoundtrip((Item) p);
            return (B) p.getBuildersList().get(before.getClass());
        } catch (Exception e) {
            throw new ShiningPandaTestException(e);
        }
    }

    /**
     * Performs a configuration round-trip testing for a builder on a matrix
     * project with a Tox axis.
     *
     * @param before The builder.
     * @return The reloaded builder.
     */
    @SuppressWarnings("unchecked")
    protected <B extends Builder> B configToxMatrixRoundtrip(B before) {
        configureAllPythons();
        try {
            MatrixProject p = j.createProject(MatrixProject.class);
            p.setAxes(new AxisList(new ToxAxis(new String[]{"py27", "py32", "pypy", "jython"})));
            p.getBuildersList().add(before);
            j.configRoundtrip((Item) p);
            return (B) p.getBuildersList().get(before.getClass());
        } catch (Exception e) {
            throw new ShiningPandaTestException(e);
        }
    }

    protected MatrixProject createMatrixProject() {
        try {
            return j.createProject(MatrixProject.class);
        } catch (IOException e) {
            throw new ShiningPandaTestException(e);
        }
    }

    protected FreeStyleProject createFreeStyleProject() {
        try {
            return j.createFreeStyleProject();
        } catch (IOException e) {
            throw new ShiningPandaTestException(e);
        }
    }

    /**
     * Search a field in the provided class and its super classes.
     *
     * @param klass The class to search in.
     * @param p     The field to search.
     * @return The field, or null if no such field.
     */
    protected Field getField(@SuppressWarnings("rawtypes") Class klass, String p) {
        while (klass != Object.class) {
            try {
                return klass.getDeclaredField(p);
            } catch (NoSuchFieldException e) {
            }
            klass = klass.getSuperclass();
        }
        return null;
    }

    /**
     * Same as assertEqualBeans, but works on protected and private fields.
     *
     * @param lhs        The initial object.
     * @param rhs        The final object.
     * @param properties The properties to check.
     * @throws Exception
     */
    public void assertEqualBeans2(Object lhs, Object rhs, String properties) throws Exception {
        assertNotNull("lhs is null", lhs);
        assertNotNull("rhs is null", rhs);
        for (String p : properties.split(",")) {
            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(lhs, p);
            Object lp, rp;
            if (pd == null) {
                Field f = getField(lhs.getClass(), p);
                assertNotNull("No such property " + p + " on " + lhs.getClass(), f);
                boolean accessible = f.isAccessible();
                if (!accessible)
                    f.setAccessible(true);
                lp = f.get(lhs);
                rp = f.get(rhs);
                f.setAccessible(accessible);
            } else {
                lp = PropertyUtils.getProperty(lhs, p);
                rp = PropertyUtils.getProperty(rhs, p);
            }

            if (lp != null && rp != null && lp.getClass().isArray() && rp.getClass().isArray()) {
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
    public File getConfigFile() {
        return new File(j.jenkins.getRootDir(), "config.xml");
    }

    /**
     * Get the configuration file for this project.
     *
     * @param project The project
     * @return The configuration file
     */
    public File getConfigFile(AbstractProject<?, ?> project) {
        return new File(j.jenkins.getRootDir(),
                StringUtils.join(new String[]{"jobs", project.getName(), "config.xml"}, File.separator));
    }

    /**
     * Create a workspace.
     *
     * @return The workspace
     */
    public Workspace getWorkspace() {
        return Workspace.fromHome(new FilePath(createTmpDir()));
    }

    /**
     * Create a master workspace.
     *
     * @return The workspace
     */
    public MasterWorkspace getMasterWorkspace() {
        return new MasterWorkspace(new FilePath(createTmpDir()));
    }

    /**
     * Create a slave workspace.
     *
     * @return The workspace
     */
    public SlaveWorkspace getSlaveWorkspace() {
        return new SlaveWorkspace(new FilePath(createTmpDir()));
    }

    /**
     * Get the packages directory.
     *
     * @return The package directory
     */
    public File getPackagesDir() {
        return new File(j.jenkins.getRootDir(), Workspace.BASENAME + File.separator + Workspace.PACKAGES);
    }

    /**
     * Create the packages directory.
     *
     * @return The packages directory
     */
    public File createPackagesDir() {
        File packagesDir = getPackagesDir();
        packagesDir.mkdirs();
        return packagesDir;
    }

    public File createTmpDir(String... parts) {
        try {
            return temporaryFolder.newFolder(StringUtils.join(parts, File.separator));
        } catch (IOException e) {
            throw new ShiningPandaTestException(e);
        }
    }

    /**
     * Convert a FilePath to a File.
     *
     * @param filePath The FilePath to convert
     * @return The resulting file
     */
    public File toFile(FilePath filePath) {
        return new File(filePath.getRemote());
    }

    /**
     * Assert that file exists
     *
     * @param file The file to check
     */
    public void assertFile(File file) {
        assertTrue("file does not exist: " + file.getAbsolutePath(), file.isFile());
    }

    /**
     * Assert that file exists
     *
     * @param filePath The file to check
     */
    public void assertFile(FilePath filePath) {
        assertFile(toFile(filePath));
    }

    /**
     * Assert that directory exists
     *
     * @param file The directory to check
     */
    public void assertDirectory(File file) {
        assertTrue("directory does not exist: " + file.getAbsolutePath(), file.isDirectory());
    }

    public void assertDirectory(FilePath filePath) {
        assertDirectory(toFile(filePath));
    }

    /**
     * Assert that file does not exist
     *
     * @param file The file to check
     */
    public void assertNotExists(File file) {
        assertFalse("file exists: " + file.getAbsolutePath(), file.exists());
    }

    public void assertNotExists(FilePath filePath) {
        assertNotExists(toFile(filePath));
    }

    public void assertContentEquals(File file1, File file2) {
        assertFile(file1);
        assertFile(file2);
        try {
            assertEquals("file content differ: " + file1.getAbsolutePath() + " != " + file2.getAbsolutePath(),
                    FileUtils.readFileToString(file1, StandardCharsets.UTF_8),
                    FileUtils.readFileToString(file2, StandardCharsets.UTF_8));
        } catch (IOException e) {
            fail("failed to read file content: " + e.getMessage());
        }
    }

    public void assertContentEquals(FilePath filePath1, FilePath filePath2) {
        assertContentEquals(toFile(filePath1), toFile(filePath2));
    }

    public FilePath getFilePath(String pathname) {
        return new FilePath(new File(pathname));
    }
}
