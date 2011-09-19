package jenkins.plugins.shiningpanda;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import org.apache.commons.io.FileUtils;

public class TestVirtualenvBuilder extends ShiningPandaTestCase
{

    public void testRoundTripFreeStyle() throws Exception
    {
        StandardPythonInstallation installation = configureCPython2();
        VirtualenvBuilder before = new VirtualenvBuilder(installation.getName(), "env2", true, false, "echo hello");
        VirtualenvBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "home,clear,useDistribute,command,pythonName");
    }

    public void testRoundTripMatrix() throws Exception
    {
        VirtualenvBuilder before = new VirtualenvBuilder("foobar", "env2", true, false, "echo hello");
        VirtualenvBuilder after = configMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "home,clear,useDistribute,command");
    }

    public void testStandardPythonHomeWithSpace() throws Exception
    {
        StandardPythonInstallation installation = configurePython("Python", "/tmp/bad move");
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "env", false, true, "echo hello");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains("Whitespace not allowed in PYTHONHOME"));
    }

    public void testVirtualenvHomeWithSpace() throws Exception
    {
        StandardPythonInstallation installation = configureCPython2();
        VirtualenvBuilder builder = new VirtualenvBuilder(installation.getName(), "bad move", false, true, "echo hello");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains("Whitespace not allowed in PYTHONHOME"));
    }
}
