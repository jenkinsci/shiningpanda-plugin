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
package jenkins.plugins.shiningpanda.command;

import junit.framework.TestCase;

public class TestUnixCommand extends TestCase {

    public void testAddFirstLine() {
	UnixCommand command = new UnixCommand("yop", true, true);
	assertEquals("\nyop", command.getContents());
    }

    public void testDoNotAddFirstLineIfAlreadyBlank() {
	String contents = "\nyop";
	UnixCommand command = new UnixCommand(contents, true, true);
	assertEquals(contents, command.getContents());
    }

    public void testReplaceAntiSlashR() {
	UnixCommand command = new UnixCommand("\nhello\r\nworld!", true, true);
	assertEquals("\nhello\nworld!", command.getContents());
    }

    public void testConvertCommand() throws Exception {
	UnixCommand command = new UnixCommand("\nHello %Who%!\ndir toto\\tutu\ndir toto%Who%yup", true, true);
	assertEquals("\nHello ${Who}!\ndir toto/tutu\ndir toto${Who}yup", command.getContents());
    }

    public void testDoNotConvertCommand() throws Exception {
	String contents = "\nHello %Who%!\ndir toto\\tutu";
	UnixCommand command = new UnixCommand(contents, true, false);
	assertEquals(contents, command.getContents());
    }

}
