#!/bin/sh
unset PYTHONHOME
unset VIRTUAL_ENV
unset JYTHON_HOME
case $1 in
    eclipse)
        mvn -DdownloadSources=true -DdownloadJavadocs=true -DoutputDirectory=target/eclipse-classes eclipse:eclipse
        ;;
    test-debug)
        mvn -Dmaven.surefire.debug -Dtest=jenkins.plugins.shiningpanda.$2
        ;;
    test)
        mvn -Dtest=jenkins.plugins.shiningpanda.$2
        ;;
    run-hosted)
        mvn clean hpi:run -Djenkins.plugins.shiningpanda.ShiningPanda.hosted=true
        ;;
    help)
        echo "usage: sp <subcommand> [args]"
        echo
        echo "Available subcommands:"
        echo "    eclipse: configure project for Eclipse,"
        echo "    test-debug <TestCase>: run this test case in debug mode."
        echo "    run-hosted: execute clean hpi:run in hosted mode."
        ;;
    *)
        echo "Invalid command: $1"
        exit 1
        ;;
esac   
