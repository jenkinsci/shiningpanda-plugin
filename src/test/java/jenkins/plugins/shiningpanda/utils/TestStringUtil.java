/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2012 ShiningPanda S.A.S.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jenkins.plugins.shiningpanda.utils;

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
