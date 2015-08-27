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
package jenkins.plugins.shiningpanda.utils;

import java.util.Map;

import hudson.Util;
import hudson.util.VariableResolver;

public class UnixVariableResolver implements VariableResolver<String> {
    /**
     * The map used to resolve variables.
     */
    private final Map<String, String> data;

    /**
     * Constructor using fields.
     * 
     * @param data
     *            The data.
     */
    public UnixVariableResolver(Map<String, String> data) {
	this.data = data;
    }

    /**
     * Resolve a variable.
     */
    public String resolve(String name) {
	// UNIX-like: blank if not found
	return Util.fixNull(data.get(name));
    }
}
