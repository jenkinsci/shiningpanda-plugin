/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2014 ShiningPanda S.A.S.
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
package jenkins.plugins.shiningpanda.matrix;

import java.util.Set;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.ShiningPandaTestCase;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

public class TestPythonAxisTree extends ShiningPandaTestCase
{

    public void testGetInterpreters() throws Exception
    {
        PythonInstallation pi1 = new PythonInstallation("CPython-2.7", "/toto", NO_PROPERTIES);
        PythonInstallation pi2 = new PythonInstallation("PyPy-1.6", "/tata", NO_PROPERTIES);
        PythonInstallation pi3 = new PythonInstallation("CPython-3.2", "/tutu", NO_PROPERTIES);
        PythonInstallation pi4 = new PythonInstallation("Foobar", "/yop", NO_PROPERTIES);
        Set<String> interpreters = new PythonAxisTree().getInterpreters(new PythonInstallation[] { pi1, pi2, pi3, pi4 });
        assertEquals(3, interpreters.size());
        assertTrue(interpreters.contains("CPython"));
        assertTrue(interpreters.contains("PyPy"));
        assertTrue(interpreters.contains(Messages.PythonAxisTree_Others()));
    }

    public void testGetInterpreter1() throws Exception
    {
        PythonInstallation pi = new PythonInstallation("CPython-2.7", "/toto", NO_PROPERTIES);
        assertEquals("CPython", new PythonAxisTree().getInterpreter(pi));
    }

    public void testGetInterpreter2() throws Exception
    {
        PythonInstallation pi = new PythonInstallation("Foobar", "/toto", NO_PROPERTIES);
        assertEquals(Messages.PythonAxisTree_Others(), new PythonAxisTree().getInterpreter(pi));
    }

    public void testGetVersion1() throws Exception
    {
        PythonInstallation pi = new PythonInstallation("CPython-2.7", "/toto", NO_PROPERTIES);
        assertEquals("2.7", new PythonAxisTree().getVersion(pi));
    }

    public void testGetVersion2() throws Exception
    {
        PythonInstallation pi = new PythonInstallation("Foobar", "/toto", NO_PROPERTIES);
        assertEquals("Foobar", new PythonAxisTree().getVersion(pi));
    }

}
