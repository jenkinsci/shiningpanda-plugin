#!/bin/sh

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
    test-debug)
        mvn $OPTIONS -Dmaven.surefire.debug -Dtest=jenkins.plugins.shiningpanda.$2
        ;;
    test)
        mvn $OPTIONS -Dtest=jenkins.plugins.shiningpanda.$2
        ;;
    run-hosted)
        mvn $OPTIONS clean hpi:run -Djenkins.plugins.shiningpanda.ShiningPanda.hosted=true
        ;;
    release)
        mvn $OPTIONS clean release:prepare release:perform -Dusername=omansion -Dpassword=$2
        ;;
    help)
        echo "usage: sp <subcommand> [args]"
        echo
        echo "Available subcommands:"
        echo "    eclipse: configure project for Eclipse,"
        echo "    test-debug <TestCase>: run this test case in debug mode,"
        echo "    run-hosted: execute clean hpi:run in hosted mode,"
        echo "    release <github password>: release the plugin."
        ;;
    *)
        echo "Invalid command: $1"
        exit 1
        ;;
esac   
