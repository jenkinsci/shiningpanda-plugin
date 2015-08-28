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
package jenkins.plugins.shiningpanda;

import hudson.model.Items;
import hudson.model.Run;
import jenkins.plugins.shiningpanda.builders.CustomPythonBuilder;
import jenkins.plugins.shiningpanda.builders.PythonBuilder;
import jenkins.plugins.shiningpanda.builders.VirtualenvBuilder;
import jenkins.plugins.shiningpanda.matrix.PythonAxis;

public class Compatibility {

    /**
     * Version 0.4 to 0.5
     */
    private static void c_0_4__0_5() {
	// CustomVirtualenvBuilder becomes CustomPythonBuilder
	Items.XSTREAM2.addCompatibilityAlias("jenkins.plugins.shiningpanda.CustomVirtualenvBuilder",
		CustomPythonBuilder.class);
	// StandardPythonBuilder becomes PythonBuilder
	Items.XSTREAM2.addCompatibilityAlias("jenkins.plugins.shiningpanda.StandardPythonBuilder", PythonBuilder.class);
	// VirtualenvBuilder is now in a builders package
	Items.XSTREAM2.addCompatibilityAlias("jenkins.plugins.shiningpanda.VirtualenvBuilder", VirtualenvBuilder.class);
	// PythonAxis is now in a matrix package. Compatibility with
	// configuration...
	Items.XSTREAM2.addCompatibilityAlias("jenkins.plugins.shiningpanda.PythonAxis", PythonAxis.class);
	// ... and with builds
	Run.XSTREAM2.addCompatibilityAlias("jenkins.plugins.shiningpanda.PythonAxis", PythonAxis.class);
    }

    /**
     * Enable compatibility.
     */
    public static void enable() {
	// 0.4 to 0.5
	c_0_4__0_5();
    }
}
