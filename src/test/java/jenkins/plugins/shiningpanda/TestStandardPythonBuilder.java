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

public class TestStandardPythonBuilder extends ShiningPandaTestCase
{

    public void testRoundTripFreeStyle() throws Exception
    {
        StandardPythonInstallation installation = configureCPython2();
        StandardPythonBuilder before = new StandardPythonBuilder(installation.getName(), true, "echo hello");
        StandardPythonBuilder after = configFreeStyleRoundtrip(before);
        assertEqualBeans2(before, after, "command,ignoreExitCode,pythonName");
    }

    public void testRoundTripMatrix() throws Exception
    {
        StandardPythonBuilder before = new StandardPythonBuilder("foobar", false, "echo hello");
        StandardPythonBuilder after = configPythonMatrixRoundtrip(before);
        assertEqualBeans2(before, after, "command,ignoreExitCode");
    }

    public void testHomeWithSpace() throws Exception
    {
        StandardPythonInstallation installation = configurePython("Python", "/tmp/bad move");
        StandardPythonBuilder builder = new StandardPythonBuilder(installation.getName(), false, "echo hello");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains(Messages.ShiningPandaUtil_PythonHomeHasWhitespace("")));
    }

    public void testTextAxisValueAvailable() throws Exception
    {
        StandardPythonInstallation installation = configureCPython2();
        StandardPythonBuilder builder = new StandardPythonBuilder(null, false, "echo \"Welcome $TOTO\"");
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
        StandardPythonInstallation installation = configureCPython2();
        String pythonName = "Toto";
        StandardPythonBuilder builder = new StandardPythonBuilder(pythonName, false, "echo \"Welcome $TOTO\"");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains(Messages.InstalledPythonBuilder_InstallationNotFound(pythonName, installation.getName())));
    }

    public void testNoPython() throws Exception
    {
        StandardPythonBuilder builder = new StandardPythonBuilder("Toto", false, "echo \"Welcome $TOTO\"");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue(log.contains(Messages.ShiningPandaUtil_PythonInstallationNotFound()));
    }

    public void testNoPythonAxis() throws Exception
    {
        configureCPython2();
        StandardPythonBuilder builder = new StandardPythonBuilder(null, false, "echo \"Welcome $TOTO\"");
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
        StandardPythonInstallation installation = configureCPython2();
        StandardPythonBuilder builder = new StandardPythonBuilder(installation.getName(), true, "ls foobartrucmuch");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("This build should have been successful", log.contains("SUCCESS"));
    }

    public void testConsiderExitCode() throws Exception
    {
        StandardPythonInstallation installation = configureCPython2();
        StandardPythonBuilder builder = new StandardPythonBuilder(installation.getName(), false, "ls foobartrucmuch");
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("This build should have failed", log.contains("FAILURE"));
    }

}
