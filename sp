#!/bin/sh
# ShiningPanda plug-in for Jenkins
# Copyright (C) 2011-2014 ShiningPanda S.A.S.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

# Clean
unset PYTHONHOME
unset VIRTUAL_ENV
unset JYTHON_HOME

# Use custom maven settings if exists
if [ -f $HOME/.m2/jenkins-settings.xml ]
then
    OPTIONS="-s $HOME/.m2/jenkins-settings.xml"
fi

# Check operation to perform
case $1 in
    eclipse)
        mvn $OPTIONS -DdownloadSources=true -DdownloadJavadocs=true -DoutputDirectory=target/eclipse-classes eclipse:eclipse
        ;;
    install)
        mvn $OPTIONS clean install
        ;;
    test)
        mvn $OPTIONS -Dtest=jenkins.plugins.shiningpanda.$2
        ;;
    test-debug)
        mvn $OPTIONS -Dmaven.surefire.debug -Dtest=jenkins.plugins.shiningpanda.$2
        ;;
    run)
        mvn $OPTIONS clean hpi:run
        ;;
    debug)
        mvnDebug $OPTIONS clean hpi:run
        ;;
    release)
        mvn $OPTIONS clean release:prepare release:perform -Darguments="-DskipTests"
        ;;
    help)
        echo "usage: sp <subcommand> [args]"
        echo
        echo "Available subcommands:"
        echo "    eclipse: configure project for Eclipse,"
        echo "    install: execute clean and install goals,"
        echo "    test <TestCase>: run this test case,"
        echo "    test-debug <TestCase>: run this test case in debug mode,"
        echo "    run: execute clean hpi:run,"
        echo "    run-hosted: execute clean hpi:run in hosted mode,"
        echo "    debug: execute clean hpi:run in debug mode,"
        echo "    debug-hosted: execute clean hpi:run in debug and hosted mode,"
        echo "    release <github password>: release the plugin."
        ;;
    *)
        echo "Invalid command: $1"
        exit 1
        ;;
esac   
