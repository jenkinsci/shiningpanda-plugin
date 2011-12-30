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
package jenkins.plugins.shiningpanda.tools;

import jenkins.plugins.shiningpanda.ShiningPandaTestCase;

public class TestPythonInstallation extends ShiningPandaTestCase
{

    public void testRoundTrip() throws Exception
    {
        PythonInstallation beforeInstallation = configureCPython2();
        configRoundtrip();
        PythonInstallation[] afterInstallations = getPythonInstallations();
        assertEquals(1, afterInstallations.length);
        PythonInstallation afterInstallation = afterInstallations[0];
        assertEqualBeans(beforeInstallation, afterInstallation, "name,home");
    }

}
