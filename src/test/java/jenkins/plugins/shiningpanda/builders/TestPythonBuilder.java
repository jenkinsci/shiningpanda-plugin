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
import jenkins.plugins.shiningpanda.matrix.PythonAxis;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

import org.apache.commons.io.FileUtils;

public class TestPythonBuilder extends ShiningPandaTestCase
{

    public void testRoundTripFreeStyle() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        PythonBuilder before = new PythonBuilder(installation.getName(), "echo hello", true);
        PythonBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "pythonName,command,ignoreExitCode");
    }

    public void testRoundTripMatrix() throws Exception
    {
        PythonBuilder before = new PythonBuilder("foobar", "echo hello", false);
        PythonBuilder after = configPythonMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "command,ignoreExitCode");
    }

    public void testHomeWithSpace() throws Exception
    {
        PythonInstallation installation = configurePython("Python", "/tmp/bad move");
        PythonBuilder builder = new PythonBuilder(installation.getName(), "echo hello", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains(Messages.ShiningPandaUtil_PythonHomeHasWhitespace("")));
    }

    public void testTextAxisValueAvailable() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        PythonBuilder builder = new PythonBuilder(null, "echo \"Welcome $TOTO\"", false);
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

    public void testInvalidPythonName() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        String pythonName = "Toto";
        PythonBuilder builder = new PythonBuilder(pythonName, "echo \"Welcome $TOTO\"", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains(Messages.InstalledPythonBuilder_InstallationNotFound(pythonName, installation.getName())));
    }

    public void testNoPython() throws Exception
    {
        PythonBuilder builder = new PythonBuilder("Toto", "echo \"Welcome $TOTO\"", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains(Messages.ShiningPandaUtil_PythonInstallationNotFound()));
    }

    public void testNoPythonAxis() throws Exception
    {
        configureCPython2();
        PythonBuilder builder = new PythonBuilder(null, "echo \"Welcome $TOTO\"", false);
        MatrixProject project = createMatrixProject();
        AxisList axes = new AxisList(new TextAxis("TOTO", "TUTU"));
        project.setAxes(axes);
        project.getBuildersList().add(builder);
        MatrixBuild build = project.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(1, runs.size());
        MatrixRun run = runs.get(0);
        String log = FileUtils.readFileToString(run.getLogFile());
        assertTrue(log.contains(Messages.ShiningPandaUtil_PythonInstallationNotFound()));
    }

    public void testIgnoreExitCode() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        PythonBuilder builder = new PythonBuilder(installation.getName(), "ls foobartrucmuch", true);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("This build should have been successful", log.contains("SUCCESS"));
    }

    public void testConsiderExitCode() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        PythonBuilder builder = new PythonBuilder(installation.getName(), "ls foobartrucmuch", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("This build should have failed", log.contains("FAILURE"));
    }

}
