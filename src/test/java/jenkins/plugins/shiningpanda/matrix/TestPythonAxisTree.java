package jenkins.plugins.shiningpanda.matrix;

import java.util.Set;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

public class TestPythonAxisTree extends ShiningPandaTestCase
{

    public void testGetInterpreters() throws Exception
    {
        PythonInstallation pi1 = new PythonInstallation("CPython-2.7", "/toto", NO_PROPERTIES);
        PythonInstallation pi2 = new PythonInstallation("PyPy-1.6", "/tata", NO_PROPERTIES);
        PythonInstallation pi3 = new PythonInstallation("CPython-3.2", "/tutu", NO_PROPERTIES);
        PythonInstallation pi4 = new PythonInstallation("Foobar", "/yop", NO_PROPERTIES);
        Set<String> interpreters = new PythonAxisTree().getInterpreters(new PythonInstallation[] { pi1, pi2, pi3, pi4 });
        assertEquals(3, interpreters.size());
        assertTrue(interpreters.contains("CPython"));
        assertTrue(interpreters.contains("PyPy"));
        assertTrue(interpreters.contains(Messages.PythonAxisTree_Others()));
    }

    public void testGetInterpreter1() throws Exception
    {
        PythonInstallation pi = new PythonInstallation("CPython-2.7", "/toto", NO_PROPERTIES);
        assertEquals("CPython", new PythonAxisTree().getInterpreter(pi));
    }

    public void testGetInterpreter2() throws Exception
    {
        PythonInstallation pi = new PythonInstallation("Foobar", "/toto", NO_PROPERTIES);
        assertEquals(Messages.PythonAxisTree_Others(), new PythonAxisTree().getInterpreter(pi));
    }

    public void testGetVersion1() throws Exception
    {
        PythonInstallation pi = new PythonInstallation("CPython-2.7", "/toto", NO_PROPERTIES);
        assertEquals("2.7", new PythonAxisTree().getVersion(pi));
    }

    public void testGetVersion2() throws Exception
    {
        PythonInstallation pi = new PythonInstallation("Foobar", "/toto", NO_PROPERTIES);
        assertEquals("Foobar", new PythonAxisTree().getVersion(pi));
    }

}
