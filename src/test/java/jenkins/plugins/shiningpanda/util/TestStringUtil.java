package jenkins.plugins.shiningpanda.util;

import junit.framework.TestCase;

public class TestStringUtil extends TestCase
{

    public void testHasWhitespace() throws Exception
    {
        assertFalse("should not have whitespace", StringUtil.hasWhitespace(null));
        assertTrue("should have whitespace", StringUtil.hasWhitespace("hello world"));
        assertTrue("should have whitespace", StringUtil.hasWhitespace("hello\tworld"));
        assertFalse("should not have whitespace", StringUtil.hasWhitespace("hello_world"));
    }

    public void testFixCrLf() throws Exception
    {
        assertEquals("\nabc\ndef\n", StringUtil.fixCrLf("\r\nabc\r\ndef\r\n"));
    }

}
