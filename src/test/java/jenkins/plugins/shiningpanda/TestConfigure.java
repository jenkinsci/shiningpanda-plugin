package jenkins.plugins.shiningpanda;

public class TestConfigure extends ShiningPandaTestCase
{

    public void testRoundTrip() throws Exception
    {
        StandardPythonInstallation beforeInstallation = configureCPython2();
        configRoundtrip();
        StandardPythonInstallation[] afterInstallations = getPythonInstallations();
        assertEquals(1, afterInstallations.length);
        StandardPythonInstallation afterInstallation = afterInstallations[0];
        assertEqualBeans(beforeInstallation, afterInstallation, "name,home");
    }

}
