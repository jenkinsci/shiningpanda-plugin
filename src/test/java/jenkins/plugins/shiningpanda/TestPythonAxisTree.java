package jenkins.plugins.shiningpanda;

import java.util.Set;

public class TestPythonAxisTree extends ShiningPandaTestCase
{

    public void testGetInterpreters() throws Exception
    {
        StandardPythonInstallation pi1 = new StandardPythonInstallation("CPython-2.7", "/toto", NO_PROPERTIES);
        StandardPythonInstallation pi2 = new StandardPythonInstallation("PyPy-1.6", "/tata", NO_PROPERTIES);
        StandardPythonInstallation pi3 = new StandardPythonInstallation("CPython-3.2", "/tutu", NO_PROPERTIES);
        StandardPythonInstallation pi4 = new StandardPythonInstallation("Foobar", "/yop", NO_PROPERTIES);
        Set<String> interpreters = new PythonAxisTree().getInterpreters(new PythonInstallation[] { pi1, pi2, pi3, pi4 });
        assertEquals(3, interpreters.size());
        assertTrue(interpreters.contains("CPython"));
        assertTrue(interpreters.contains("PyPy"));
        assertTrue(interpreters.contains(Messages.PythonAxisTree_Others()));
    }

    public void testGetInterpreter1() throws Exception
    {
        StandardPythonInstallation pi = new StandardPythonInstallation("CPython-2.7", "/toto", NO_PROPERTIES);
        assertEquals("CPython", new PythonAxisTree().getInterpreter(pi));
    }

    public void testGetInterpreter2() throws Exception
    {
        StandardPythonInstallation pi = new StandardPythonInstallation("Foobar", "/toto", NO_PROPERTIES);
        assertEquals(Messages.PythonAxisTree_Others(), new PythonAxisTree().getInterpreter(pi));
    }

    public void testGetVersion1() throws Exception
    {
        StandardPythonInstallation pi = new StandardPythonInstallation("CPython-2.7", "/toto", NO_PROPERTIES);
        assertEquals("2.7", new PythonAxisTree().getVersion(pi));
    }

    public void testGetVersion2() throws Exception
    {
        StandardPythonInstallation pi = new StandardPythonInstallation("Foobar", "/toto", NO_PROPERTIES);
        assertEquals("Foobar", new PythonAxisTree().getVersion(pi));
    }

}
