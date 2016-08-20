#!/usr/bin/env bash
mvn clean && mvn install
mvn exec:java -Dexec.mainClass="de.citec.sc.matoll.process.Matoll_Baseline" -Dexec.args="--mode=train /Users/swalter/Downloads/input_EN_cleaned/ /Users/swalter/Desktop/config.xml"
