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

import java.util.regex.Pattern;

public class StringUtil {

    /**
     * Fix CR/LF and always make it Unix style
     *
     * @param s The string to fix
     * @return The fixed string
     */
    public static String fixCrLf(String s) {
        int idx;
        while ((idx = s.indexOf("\r\n")) != -1)
            s = s.substring(0, idx) + s.substring(idx + 1);
        return s;
    }

    /**
     * Check that the provided value has no whitespace.
     *
     * @param value The value to check.
     * @return True if has whitespace, else false
     */
    public static boolean hasWhitespace(String value) {
        if (value == null)
            return false;
        return Pattern.compile("\\s").matcher(value).find();
    }

    /**
     * Check that the provided array is not null. If null fix it by returning an
     * empty array.
     *
     * @param values The array to fix
     * @return A fixed array
     */
    public static String[] fixNull(String[] values) {
        return values != null ? values : new String[]{};
    }

}
