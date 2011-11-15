package jenkins.plugins.shiningpanda.builders;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import java.util.List;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.ShiningPandaTestCase;

import org.apache.commons.io.FileUtils;

public class TestCustomPythonBuilder extends ShiningPandaTestCase
{

    public void testRoundTripFreeStyle() throws Exception
    {
        CustomPythonBuilder before = new CustomPythonBuilder("/tmp/custom", "echo hello", true);
        CustomPythonBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "home,command,ignoreExitCode");
    }

    public void testRoundTripMatrix() throws Exception
    {
        CustomPythonBuilder before = new CustomPythonBuilder("/tmp/custom", "echo hello", false);
        CustomPythonBuilder after = configPythonMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "home,command,ignoreExitCode");
    }

    public void testHomeWithSpace() throws Exception
    {
        CustomPythonBuilder builder = new CustomPythonBuilder(createVirtualenv(getTempDir("bad move")).getAbsolutePath(),
                "echo hello", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains(Messages.ShiningPandaUtil_PythonHomeHasWhitespace("")));
    }

    public void testTextAxisAvailable() throws Exception
    {
        CustomPythonBuilder builder = new CustomPythonBuilder(getCPython2Home(), "echo \"Welcome $TOTO\"", false);
        MatrixProject project = createMatrixProject();
        AxisList axes = new AxisList(new TextAxis("TOTO", "TUTU"));
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
        CustomPythonBuilder builder = new CustomPythonBuilder(getCPython3Home(), "ls foobartrucmuch", true);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("This build should have been successful", log.contains("SUCCESS"));
    }

    public void testConsiderExitCode() throws Exception
    {
        CustomPythonBuilder builder = new CustomPythonBuilder(getJythonHome(), "ls foobartrucmuch", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("This build should have failed", log.contains("FAILURE"));
    }

}
