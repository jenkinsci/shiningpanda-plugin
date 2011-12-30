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
