/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2015 ShiningPanda S.A.S.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of its license which incorporates the terms and 
 * conditions of version 3 of the GNU Affero General Public License, 
 * supplemented by the additional permissions under the GNU Affero GPL
 * version 3 section 7: if you modify this program, or any covered work, 
 * by linking or combining it with other code, such other code is not 
 * for that reason alone subject to any of the requirements of the GNU
 * Affero GPL version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * license for more details.
 *
 * You should have received a copy of the license along with this program.
 * If not, see <https://raw.github.com/jenkinsci/shiningpanda-plugin/master/LICENSE.txt>.
 */
package jenkins.plugins.shiningpanda.utils;

import junit.framework.TestCase;

public class TestStringUtil extends TestCase {

    public void testHasWhitespace() throws Exception {
	assertFalse("should not have whitespace", StringUtil.hasWhitespace(null));
	assertTrue("should have whitespace", StringUtil.hasWhitespace("hello world"));
	assertTrue("should have whitespace", StringUtil.hasWhitespace("hello\tworld"));
	assertFalse("should not have whitespace", StringUtil.hasWhitespace("hello_world"));
    }

    public void testFixCrLf() throws Exception {
	assertEquals("\nabc\ndef\n", StringUtil.fixCrLf("\r\nabc\r\ndef\r\n"));
    }

}
