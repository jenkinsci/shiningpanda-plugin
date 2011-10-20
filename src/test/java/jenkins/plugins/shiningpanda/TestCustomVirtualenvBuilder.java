package jenkins.plugins.shiningpanda;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import java.util.List;

import org.apache.commons.io.FileUtils;

public class TestCustomVirtualenvBuilder extends ShiningPandaTestCase
{

    public void testRoundTripFreeStyle() throws Exception
    {
        CustomVirtualenvBuilder before = new CustomVirtualenvBuilder("/tmp/custom", true, "echo hello");
        CustomVirtualenvBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "home,ignoreExitCode,command");
    }

    public void testRoundTripMatrix() throws Exception
    {
        CustomVirtualenvBuilder before = new CustomVirtualenvBuilder("/tmp/custom", false, "echo hello");
        CustomVirtualenvBuilder after = configMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "home,ignoreExitCode,command");
    }

    public void testHomeWithSpace() throws Exception
    {
        CustomVirtualenvBuilder builder = new CustomVirtualenvBuilder("/tmp/bad move", false, "echo hello");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains(Messages.ShiningPandaUtil_PythonHomeHasWhitespace("")));
    }

    public void testTextAxisAvailable() throws Exception
    {
        StandardPythonInstallation installation = configureCPython2();
        CustomVirtualenvBuilder builder = new CustomVirtualenvBuilder(createVirtualenv().getAbsolutePath(), false,
                "echo \"Welcome $TOTO\"");
        MatrixProject project = createMatrixProject();
        AxisList axes = new AxisList(new PythonAxis(new String[] { installation.getName(), }), new TextAxis("TOTO", "TUTU"));
        project.setAxes(axes);
        project.getBuildersList().add(builder);
        MatrixBuild build = project.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(1, runs.size());
        MatrixRun run = runs.get(0);
        String log = FileUtils.readFileToString(run.getLogFile());
        assertTrue("TextAxis value not available in builder", log.contains("Welcome TUTU"));
    }

    public void testIgnoreExitCode() throws Exception
    {
        CustomVirtualenvBuilder builder = new CustomVirtualenvBuilder(createVirtualenv().getAbsolutePath(), true,
                "ls foobartrucmuch");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("This build should have been successful", log.contains("SUCCESS"));
    }

    public void testConsiderExitCode() throws Exception
    {
        CustomVirtualenvBuilder builder = new CustomVirtualenvBuilder(createVirtualenv().getAbsolutePath(), false,
                "ls foobartrucmuch");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("This build should have failed", log.contains("FAILURE"));
    }

}
