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

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;

public class LauncherUtil {

    /**
     * Launch a process.
     *
     * @param launcher    The launcher
     * @param listener    The build listener
     * @param pwd         The working directory
     * @param environment The environment
     * @param args        The arguments
     * @return true if was successful, else false
     * @throws InterruptedException
     */
    public static boolean launch(Launcher launcher, TaskListener listener, FilePath pwd, EnvVars environment,
                                 ArgumentListBuilder args) throws InterruptedException {
        // Be able to display error
        try {
            // Launch the process
            return 0 == launcher.launch().cmds(FilePathUtil.isUnix(pwd) ? args : args.toWindowsCommand())
                    .envs(environment).stdout(listener).pwd(pwd).join();
        } catch (IOException e) {
            // Something went wrong, display error
            Util.displayIOException(e, listener);
            // Log error message
            e.printStackTrace(listener.fatalError("command execution failed"));
            // Return an error
            return false;
        }
    }

    /**
     * Create a symbolic link.
     *
     * @param launcher The launcher
     * @param listener The listener
     * @param target   The target file
     * @param link     The link file
     * @return true if successful, else false
     * @throws InterruptedException
     * @throws IOException
     */
    public static boolean createSymlink(Launcher launcher, TaskListener listener, FilePath target, FilePath link)
            throws InterruptedException, IOException {
        // Get the arguments
        ArgumentListBuilder args = new ArgumentListBuilder("ln", "-s", target.getRemote(), link.getRemote());
        // Be able to display error
        try {
            // Launch the process
            return 0 == launcher.launch().cmds(args).stdout(listener).join();
        } catch (IOException e) {
            // Something went wrong, display error
            Util.displayIOException(e, listener);
            // Log error message
            e.printStackTrace(listener.fatalError("command execution failed"));
            // Return an error
            return false;
        }
    }
}
