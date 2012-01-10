package jenkins.plugins.shiningpanda.command;

import junit.framework.TestCase;

public class TestUnixCommand extends TestCase
{

    public void testAddFirstLine()
    {
        UnixCommand command = new UnixCommand("yop", true, true);
        assertEquals("\nyop", command.getContents());
    }

    public void testDoNotAddFirstLineIfAlreadyBlank()
    {
        String contents = "\nyop";
        UnixCommand command = new UnixCommand(contents, true, true);
        assertEquals(contents, command.getContents());
    }

    public void testReplaceAntiSlashR()
    {
        UnixCommand command = new UnixCommand("\nhello\r\nworld!", true, true);
        assertEquals("\nhello\nworld!", command.getContents());
    }

    public void testConvertCommand() throws Exception
    {
        UnixCommand command = new UnixCommand("\nHello %Who%!\ndir toto\\tutu\ndir toto%Who%yup", true, true);
        assertEquals("\nHello ${Who}!\ndir toto/tutu\ndir toto${Who}yup", command.getContents());
    }

    public void testDoNotConvertCommand() throws Exception
    {
        String contents = "\nHello %Who%!\ndir toto\\tutu";
        UnixCommand command = new UnixCommand(contents, true, false);
        assertEquals(contents, command.getContents());
    }

}
