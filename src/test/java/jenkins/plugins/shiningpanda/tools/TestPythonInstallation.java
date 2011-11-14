package jenkins.plugins.shiningpanda.tools;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;

public class TestPythonInstallation extends ShiningPandaTestCase
{

    public void testRoundTrip() throws Exception
    {
        PythonInstallation beforeInstallation = configureCPython2();
        configRoundtrip();
        PythonInstallation[] afterInstallations = getPythonInstallations();
        assertEquals(1, afterInstallations.length);
        PythonInstallation afterInstallation = afterInstallations[0];
        assertEqualBeans(beforeInstallation, afterInstallation, "name,home");
    }

}
