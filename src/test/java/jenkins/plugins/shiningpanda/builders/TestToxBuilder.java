package jenkins.plugins.shiningpanda.builders;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.builders.ToxBuilder;

public class TestToxBuilder extends ShiningPandaTestCase
{

    public void testRoundTrip() throws Exception
    {
        ToxBuilder before = new ToxBuilder("toto/tox.ini", true);
        ToxBuilder after = configToxMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "toxIni,recreate");
    }

}
