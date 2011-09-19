package jenkins.plugins.shiningpanda;

public class TestStandardPythonBuilder extends ShiningPandaTestCase
{

    public void testRoundTripFreeStyle() throws Exception
    {
        StandardPythonInstallation installation = configureCPython2();
        StandardPythonBuilder before = new StandardPythonBuilder(installation.getName(), "echo hello");
        StandardPythonBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "command,pythonName");
    }

    public void testRoundTripMatrix() throws Exception
    {
        StandardPythonBuilder before = new StandardPythonBuilder("foobar", "echo hello");
        StandardPythonBuilder after = configMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "command");
    }
}
