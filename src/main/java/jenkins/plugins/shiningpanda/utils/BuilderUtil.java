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
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.command.Command;
import jenkins.plugins.shiningpanda.command.CommandNature;
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.interpreters.Virtualenv;
import jenkins.plugins.shiningpanda.matrix.PythonAxis;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuilderUtil {
    public static EnvVars getEnvironment(AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException {
        // Get the base environment
        EnvVars environment = build.getEnvironment(listener);
        // Add build variables, for instance if user defined a text axis
        environment.overrideAll(build.getBuildVariables());
        // Check if define some nasty variables
        for (String key : EnvVarsUtil.getPythonHomeKeys())
            // Check if key is contained
            if (environment.containsKey(key)) {
                // Log the error
                listener.fatalError(Messages.BuilderUtil_PythonHomeKeyFound(key));
                // Notify to do not continue the build
                return null;
            }
        // Return the consolidated environment
        return environment;
    }

    public static PythonInstallation getInstallation(AbstractBuild<?, ?> build, BuildListener listener,
                                                     EnvVars environment, String name) throws IOException, InterruptedException {
        // Check if this is a matrix build
        if (build instanceof MatrixRun) {
            // Check if the environment contains a PYTHON axis key
            if (!environment.containsKey(PythonAxis.KEY)) {
                // If not, log
                listener.fatalError(Messages.BuilderUtil_PythonAxis_Required());
                // Return null to stop the build
                return null;
            }
            // Get the name from the environment
            name = environment.get(PythonAxis.KEY);
        }
        // Check if the name exists
        if (name == null) {
            // Log the error
            listener.fatalError(Messages.BuilderUtil_Installation_NameNotFound());
            // Do not continue the build
            return null;
        }
        // Expand the HOME folder with these variables
        PythonInstallation installation = PythonInstallation.fromName(name);
        // Check if found an installation
        if (installation == null) {
            // Log
            listener.fatalError(Messages.BuilderUtil_Installation_NotFound(name));
            // Failed to find the installation, do not continue
            return null;
        }
        // Get the installation for this build
        return installation.forBuild(listener, environment);
    }

    public static Python getInterpreter(Launcher launcher, BuildListener listener, String home)
            throws IOException, InterruptedException {
        // Get an interpreter given its home
        Python interpreter = Python.fromHome(new FilePath(launcher.getChannel(), home));
        // Check if found an interpreter and if this interpreter is valid
        if (interpreter == null || !interpreter.isValid()) {
            // Log
            listener.fatalError(Messages
                    .BuilderUtil_Interpreter_Invalid(interpreter == null ? home : interpreter.getHome().getRemote()));
            // Invalid
            return null;
        }
        // Check if has white space in its home path
        if (StringUtil.hasWhitespace(interpreter.getHome().getRemote())) {
            // Log
            listener.fatalError(Messages.BuilderUtil_Interpreter_WhitespaceNotAllowed(
                    interpreter == null ? home : interpreter.getHome().getRemote()));
            // Invalid
            return null;
        }
        // This is a valid interpreter
        return interpreter;
    }

    public static Virtualenv getVirtualenv(BuildListener listener, FilePath home)
            throws IOException, InterruptedException {
        // Create the VIRTUAL environment
        Virtualenv virtualenv = new Virtualenv(home);
        // Check if has white space in its home path
        if (StringUtil.hasWhitespace(virtualenv.getHome().getRemote())) {
            // Log
            listener.fatalError(
                    Messages.BuilderUtil_Interpreter_WhitespaceNotAllowed(virtualenv.getHome().getRemote()));
            // Invalid
            return null;
        }
        // Return the VIRTUALENV
        return virtualenv;
    }

    public static boolean launch(Launcher launcher, BuildListener listener, FilePath pwd, EnvVars environment,
                                 Python interpreter, String nature, String command, boolean ignoreExitCode)
            throws IOException, InterruptedException {
        // Get PYTHON executable
        String executable = interpreter.getExecutable().getRemote();
        // Set the interpreter environment
        environment.overrideAll(interpreter.getEnvironment());
        // Add PYTHON_EXE environment variable
        environment.override("PYTHON_EXE", executable);
        // Launch the script
        return Command.get(FilePathUtil.isUnix(pwd), executable, CommandNature.get(nature), command, ignoreExitCode)
                .launch(launcher, listener, environment, pwd);
    }

    public static Python getInterpreter(Launcher launcher, BuildListener listener, EnvVars environment)
            throws IOException, InterruptedException {
        // Get the list of existing interpreter
        List<Python> interpreters = getInterpreters(launcher, listener, environment);
        // Check if at least one found
        if (!interpreters.isEmpty())
            // Return the first one
            return interpreters.get(0);
        // Failed to found one
        listener.fatalError(Messages.BuilderUtil_NoInterpreterFound());
        // Return null
        return null;
    }

    public static List<Python> getInterpreters(Launcher launcher, BuildListener listener, EnvVars environment)
            throws IOException, InterruptedException {
        // Create the interpreter list
        List<Python> interpreters = new ArrayList<Python>();
        // Go threw all PYTHON installations
        for (PythonInstallation installation : PythonInstallation.list()) {
            // Convert for the build
            installation = installation.forBuild(listener, environment);
            // Get an interpreter given its home
            Python interpreter = Python.fromHome(new FilePath(launcher.getChannel(), installation.getHome()));
            // Check if exists, is valid and has no whitespace in its home
            if (interpreter != null && interpreter.isValid()
                    && !StringUtil.hasWhitespace(interpreter.getHome().getRemote()))
                // Add the interpreter
                interpreters.add(interpreter);
        }
        // Return the list of interpreters
        return interpreters;
    }
}
