#!/bin/sh

cd /Users/gestalt/Documents/msmexplorer_git/msmexplorer/MSMExplorer/
ant
if [ $? == 0 ]; then
	cp ./dist/MSMExplorer.jar ./MSMExplorer.jar
	java -enableassertions -jar ./MSMExplorer.jar
fi

