package jenkins.plugins.shiningpanda.command;

import junit.framework.TestCase;

public class TestWindowsCommand extends TestCase
{

    public void testConvertCommand() throws Exception
    {
        WindowsCommand command = new WindowsCommand("Hello ${Who} and $Who!\nls toto/tutu", true, true);
        assertEquals("Hello %Who% and %Who%!\nls toto\\tutu\r\nexit %ERRORLEVEL%", command.getContents());
    }

    public void testDoNotConvertCommand() throws Exception
    {
        String contents = "Hello ${Who} and $Who2!\nls toto/tutu";
        WindowsCommand command = new WindowsCommand(contents, true, false);
        assertEquals(contents + "\r\nexit %ERRORLEVEL%", command.getContents());
    }

}
