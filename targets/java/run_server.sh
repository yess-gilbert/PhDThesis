#!/bin/bash
set -e

java -cp target/javassist-1.0-SNAPSHOT-jar-with-dependencies.jar \
    io.crowbar.sandbox.AgentServer 2>&1
