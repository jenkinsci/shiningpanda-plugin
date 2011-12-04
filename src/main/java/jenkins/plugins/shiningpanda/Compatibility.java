package jenkins.plugins.shiningpanda;

import hudson.model.Items;
import hudson.model.Run;
import jenkins.plugins.shiningpanda.builders.CustomPythonBuilder;
import jenkins.plugins.shiningpanda.builders.PythonBuilder;
import jenkins.plugins.shiningpanda.builders.VirtualenvBuilder;
import jenkins.plugins.shiningpanda.matrix.PythonAxis;

public class Compatibility
{

    /**
     * Version 0.4 to 0.5
     */
    private static void c_0_4__0_5()
    {
        // CustomVirtualenvBuilder becomes CustomPythonBuilder
        Items.XSTREAM2.addCompatibilityAlias("jenkins.plugins.shiningpanda.CustomVirtualenvBuilder", CustomPythonBuilder.class);
        // StandardPythonBuilder becomes PythonBuilder
        Items.XSTREAM2.addCompatibilityAlias("jenkins.plugins.shiningpanda.StandardPythonBuilder", PythonBuilder.class);
        // VirtualenvBuilder is now in a builders package
        Items.XSTREAM2.addCompatibilityAlias("jenkins.plugins.shiningpanda.VirtualenvBuilder", VirtualenvBuilder.class);
        // PythonAxis is now in a matrix package. Compatibility with
        // configuration...
        Items.XSTREAM2.addCompatibilityAlias("jenkins.plugins.shiningpanda.PythonAxis", PythonAxis.class);
        // ... and with builds
        Run.XSTREAM2.addCompatibilityAlias("jenkins.plugins.shiningpanda.PythonAxis", PythonAxis.class);
    }

    /**
     * Enable compatibility.
     */
    public static void enable()
    {
        // 0.4 to 0.5
        c_0_4__0_5();
    }
}
