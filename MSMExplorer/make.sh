#!/bin/sh

cd /Users/gestalt/Documents/msmexplorer_git/msmexplorer/MSMExplorer/
ant
if [ $? == 0 ]; then
	java -enableassertions -jar ./dist/MSMExplorer.jar
fi

