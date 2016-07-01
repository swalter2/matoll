#!/usr/bin/env bash
mvn clean && mvn install
mvn exec:java -Dexec.mainClass="de.citec.sc.matoll.process.Matoll_CreateMax" -Dexec.args="--mode=train /Users/swalter/blub/ /Users/swalter/Desktop/config.xml"
