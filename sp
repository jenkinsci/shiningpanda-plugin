#!/bin/sh
case $1 in
    eclipse)
        mvn -DdownloadSources=true -DdownloadJavadocs=true -DoutputDirectory=target/eclipse-classes eclipse:eclipse
        ;;
    debug)
        mvn -Dmaven.surefire.debug -Dtest=jenkins.plugins.shiningpanda.$2
        ;;
    help)
        echo "usage: sp <subcommand> [args]"
        echo
        echo "Available subcommands:"
        echo "    eclipse: configure project for Eclipse,"
        echo "    debug <TestCase>: run this test case in debug mode."
        ;;
    *)
        echo "Invalid command: $1"
        exit 1
        ;;
esac   
