#This is a simple script to convert EMMA output into output MSMExplorer expects. 
#All that needs to be done is for the dense matrix file header to be eliminated
#and for the sparse matrix header to go from SPARSE <numrows> <numcols>
#to <numrows> <numcols> <numedges>
#the script also converts file extensions to what MSMExplorer expects
#in particular, mtx for sparse

import sys

args = sys.argv

#NOTE: if your input is data.ascii
#this will clobber data.converted.ascii
if len(args) > 1:
	args.pop(0)
	for arg in args:
		parted = arg.rpartition('.')
		name = parted[0] + parted[1] + "converted." 
		with open(arg) as file:
			line = file.readline()
			if line.startswith('DENSE'):
				with open(name + 'dat', 'w') as output: 
					output.write(file.read())
			elif line.startswith('SPARSE'):
				with open(name + 'mtx', 'w') as output:
					lines = file.readlines()
					numedges = len(lines)
					header = line.rstrip().split(' ')
					output.write(header[1] + ' ' + header[2] + ' ' + str(numedges) + '\n')
					output.writelines(lines)
else:
	print 'emmaConvert requires at least one input file'
	sys.exit(1)

