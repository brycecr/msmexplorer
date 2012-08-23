# This class takes care of your classpath and
# simply launches an instance of MSMExplorer on Jython.
# Jython required to run. Note that Jython does not
# support C-extensions

import sys

def setClassPath():
	libDir = "/Users/gestalt/Documents/msmexplorer_git/msmexplorer/MSMExplorer/"
	classPaths = [
		"lib/batik-ext.jar",
		"lib/batik-xml.jar",
		"lib/prefuse.jar",
		"lib/batik-awt-util.jar",
		"lib/batik-svggen.jar",
		"lib/commons-math-2.1.jar",
		"lib/batik-codec.jar",
		"lib/batik-swing.jar",		
		"lib/lucene-1.4.3.jar",	
		"lib/swing-layout-1.0.4.jar",
		"lib/batik-dom.jar",
		"lib/batik-util.jar",
		"lib/mtj-0.9.12.jar",
		"dist/MSMExplorer.jar"
	]
	for classPath in classPaths:
		sys.path.append(libDir+classPath)

def runJavaClass():
	from edu.stanford.folding.msmexplorer import MSMExplorer
	me = MSMExplorer.main(["hi"])

def main():
	setClassPath()
	runJavaClass()

if __name__ == "__main__":
	main()
