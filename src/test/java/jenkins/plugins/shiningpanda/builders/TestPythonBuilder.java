package jenkins.plugins.shiningpanda.builders;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import java.io.File;
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
        File virtualenv = createVirtualenv(createTmpDir("bad move"));
        PythonInstallation installation = configurePython("Python", virtualenv.getAbsolutePath());
        PythonBuilder builder = new PythonBuilder(installation.getName(), "echo hello", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("whitespace should not have been allowed:\n" + log,
                log.contains(Messages.BuilderUtil_Interpreter_WhitespaceNotAllowed("")));
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
        assertTrue("value of text axis not available in builder:\n" + log, log.contains("Welcome TUTU"));
    }

    public void testInvalidPythonName() throws Exception
    {
        configureCPython2();
        String name = "Toto";
        PythonBuilder builder = new PythonBuilder(name, "echo \"Welcome $TOTO\"", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("an invalid installation name was expected:\n" + log,
                log.contains(Messages.BuilderUtil_Installation_NotFound(name)));
    }

    public void testNoInstallation() throws Exception
    {
        String name = "Toto";
        PythonBuilder builder = new PythonBuilder(name, "echo \"Welcome $TOTO\"", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("a missing installation was expected:\n" + log,
                log.contains(Messages.BuilderUtil_Installation_NotFound(name)));
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
        assertTrue("a missing python axis was expected:\n" + log, log.contains(Messages.BuilderUtil_PythonAxis_Required()));
    }

    public void testIgnoreExitCode() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        PythonBuilder builder = new PythonBuilder(installation.getName(), "ls foobartrucmuch", true);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have been successful:\n" + log, log.contains("SUCCESS"));
    }

    public void testConsiderExitCode() throws Exception
    {
        PythonInstallation installation = configureCPython2();
        PythonBuilder builder = new PythonBuilder(installation.getName(), "ls foobartrucmuch", false);
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String log = FileUtils.readFileToString(build.getLogFile());
        assertTrue("this build should have failed:\n" + log, log.contains("FAILURE"));
    }

}
