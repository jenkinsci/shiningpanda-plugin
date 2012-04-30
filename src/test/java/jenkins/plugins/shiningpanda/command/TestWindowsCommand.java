/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2012 ShiningPanda S.A.S.
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
