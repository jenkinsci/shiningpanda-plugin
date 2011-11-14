/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011 ShiningPanda S.A.S.
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
package jenkins.plugins.shiningpanda.util;

import java.util.regex.Pattern;

public class StringUtil
{

    /**
     * Fix CR/LF and always make it Unix style
     * 
     * @param s
     *            The string to fix
     * @return The fixed string
     */
    public static String fixCrLf(String s)
    {
        int idx;
        while ((idx = s.indexOf("\r\n")) != -1)
            s = s.substring(0, idx) + s.substring(idx + 1);
        return s;
    }

    /**
     * Check that the provided value has no whitespace.
     * 
     * @param value
     *            The value to check.
     * @return True if has whitespace, else false
     */
    public static boolean hasWhitespace(String value)
    {
        if (value == null)
            return false;
        return Pattern.compile("\\s").matcher(value).find();
    }

}
