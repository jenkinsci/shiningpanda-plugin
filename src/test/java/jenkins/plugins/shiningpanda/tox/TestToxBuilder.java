package jenkins.plugins.shiningpanda.tox;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;

public class TestToxBuilder extends ShiningPandaTestCase
{

    public void testRoundTrip() throws Exception
    {
        ToxBuilder before = new ToxBuilder("toto/tox.ini", "-v --with-xunit", true);
        ToxBuilder after = configToxMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "toxIni,posArgs,recreate");
    }

}
