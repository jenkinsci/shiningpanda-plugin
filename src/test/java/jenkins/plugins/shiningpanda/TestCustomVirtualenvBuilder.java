package jenkins.plugins.shiningpanda;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import org.apache.commons.io.FileUtils;

public class TestCustomVirtualenvBuilder extends ShiningPandaTestCase
{

    public void testRoundTripFreeStyle() throws Exception
    {
        CustomVirtualenvBuilder before = new CustomVirtualenvBuilder("/tmp/custom", "echo hello");
        CustomVirtualenvBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "home,command");
    }

    public void testRoundTripMatrix() throws Exception
    {
        CustomVirtualenvBuilder before = new CustomVirtualenvBuilder("/tmp/custom", "echo hello");
        CustomVirtualenvBuilder after = configMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "home,command");
    }

    public void testHomeWithSpace() throws Exception
    {
        CustomVirtualenvBuilder builder = new CustomVirtualenvBuilder("/tmp/bad move", "echo hello");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains("Whitespace not allowed in PYTHONHOME"));
    }
}
