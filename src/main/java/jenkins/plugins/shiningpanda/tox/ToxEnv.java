package jenkins.plugins.shiningpanda.tox;

import java.util.Arrays;
import java.util.List;

public class ToxEnv
{

    /**
     * Python 2.4
     */
    public static String py24 = "py24";

    /**
     * Python 2.5
     */
    public static String py25 = "py25";

    /**
     * Python 2.6
     */
    public static String py26 = "py26";

    /**
     * Python 2.7
     */
    public static String py27 = "py27";

    /**
     * Python 3.0
     */
    public static String py30 = "py30";

    /**
     * Python 3.1
     */
    public static String py31 = "py31";

    /**
     * Python 3.2
     */
    public static String py32 = "py32";

    /**
     * Jython
     */
    public static String jython = "jython";

    /**
     * PyPy
     */
    public static String pypy = "pypy";

    /**
     * Tox's default test environments
     */
    public static List<String> defaults = Arrays
            .asList(new String[] { py24, py25, py26, py27, py30, py31, py32, jython, pypy });

}
