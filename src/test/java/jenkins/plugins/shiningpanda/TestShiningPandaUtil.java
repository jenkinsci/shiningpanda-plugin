package jenkins.plugins.shiningpanda;

import junit.framework.TestCase;

public class TestShiningPandaUtil extends TestCase
{

    public void testHasWhitespace() throws Exception
    {
        assertFalse("should not have whitespace", ShiningPandaUtil.hasWhitespace(null));
        assertTrue("should have whitespace", ShiningPandaUtil.hasWhitespace("hello world"));
        assertTrue("should have whitespace", ShiningPandaUtil.hasWhitespace("hello\tworld"));
        assertFalse("should not have whitespace", ShiningPandaUtil.hasWhitespace("hello_world"));
    }

}
