/* file: main.cpp
 * program: MSMtoGML
 * -------------------------
 * Author: Bryce Cronkite-Ratcliff
 * email: brycecr@stanford.edu
 * 
 * This program intends to convert
 * MSMBuilder output into GraphMl xml-based
 * format for use with MSMExplorer. 
 * Motivation for convserion: MSMExplorer
 * is based on the Prefuse visualization package,
 * which is able to handle and read GraphMl
 * automatically.
 */

/* Version Information
 *
 * Version: aplha, 04
 * Date: June 30th, 2010
 * capable of converting
 * Dot, mtx, dat. For dot,
 * includes label and eqProb
 * attributes. For dat and mtx,
 * asks for a file containing
 * eqProb data and, if such a 
 * file is provided, adds that
 * to the output as a node field.
 *
 * Version: alpha, 03
 * Date: June 24th, 2010.
 * Can convert .dot and .dat type
 * into simple GraphML with
 * one node-bound data schema ("label")
 * and one edge-bound data schema ("probability")
 *
 *
 * Version: alpha, 03
 * Date: June 23rd, 2010.
 * Can convert .dot and .dat type
 * into simple GraphML with
 * one node-bound data schema ("label").
 *
 *
 * Version: alpha, 02
 * Date: June 22nd, 2010.
 * Can interpret .dat and create
 * a simple GraphML file. 
 *
 *
 * Version: alpha, 01:
 * Date: June 21st 3:00 PM, 2010
 * Can interepret a .dat simple matrix
 * with no header and regurgitate its
 * contents into a file "ConversionText.txt"
 */

#include <iostream>
#include <algorithm>
#include <iterator>
#include <vector>
#include <fstream>
#include <sstream>

using namespace std;

#define ALPHA_OUTPUT "conversionText.xml"

//TODO: Fix Tabbing


float StringToFloat(string str)
{
	stringstream sstream;
	sstream << str;
	float converted;
	sstream >> converted;
	return converted;
}

// Takes in an ifstream by reference
//and makes sure it is valid; if it is not
// reprompts until quit is given
void GetStream(ifstream & infile, string & filename)
{
	while (true)
	{
		cout << "Enter the filename you wish to convert," << endl;
		cout << "or enter q to exit" << endl;
		
		getline(cin, filename);
		
		if (filename == "q" || filename == "quit")
			exit(0);
		
		infile.open(filename.c_str());
		
		if ( infile.is_open() ) break;
		
		cerr << "Unable to open file " << filename << endl;
	}
}

void GetEPStream(ifstream & epIn)
{
	cout << "Do you want to supply a file containing Equlibrium Probabilities?" << endl;
	cout << "Enter \"no\" or the name of the file you wish to convert" << endl;
	
	string epFile;
	getline(cin, epFile);
	
	if (epFile == "no" || epFile == "n")
		return;
	
	while (true)
	{
		cout << "Enter the Equilibrium Probabilities file," << endl;
		cout << "or enter q to exit" << endl;
		
		getline(cin, epFile);
		
		if (epFile == "q" || epFile == "quit")
			exit(0);
		
		epIn.open(epFile.c_str());
		
		if ( epIn.is_open() ) break;
		
		cerr << "Unable to open file " << epFile << endl;
	}
	
	
	
}

class PrintCol {
public:
	explicit PrintCol(ofstream & of): outfile(of)
	{
	}
	
	void operator() (vector<float> & currCol)
	{
		ostream_iterator<float> out(outfile, " ");
		copy(currCol.begin(), currCol.end(), out);
		outfile << endl;
	}
private:
	ofstream & outfile;
};

void PrintHeader(ofstream & outfile)
{
	outfile << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" << endl;
	outfile << "<!-- This file converted from MSMBuilder Output by MSMtoGML -->" << endl;
	outfile << "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">" << endl;
	outfile << "<graph edgedefault=\"directed\">" << endl;
	
}

void PrintDataSchema(ofstream & outfile,  vector<float> & eqProbs)
{
	//label field is necessary for label rendering in prefuse
	outfile << "<key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>" << endl; 
	outfile << "<key id=\"probability\" for=\"edge\" attr.name=\"probability\" attr.type=\"double\"/>" << endl;
	if (!eqProbs.empty())
		outfile << "<key id=\"eqProb\" for =\"node\" attr.name=\"eqProb\" attr.type=\"double\"/>" << endl;
	//TODO: determine what is required
}

void PrintNodes(ofstream & outfile, int numStates,  vector<float> & eqProbs, int offset)
{
	outfile << "<!-- Nodes -->" << endl;
	bool hasEqProb = !(eqProbs.empty());
	for (size_t i = 0; i < numStates; i++)
	{
		outfile << "<node id=\"" << i + offset << "\">" << endl;
		outfile << "\t<data key=\"label\">" << i + offset << "</data>" << endl;
		if (hasEqProb) 
			outfile << "\t<data key=\"eqProb\">" << eqProbs[i] << "</data>" << endl;
		outfile << "</node>" << endl;
	}
}

void PrintEdges(ofstream & outfile,  vector<vector<float> > & rows)
{
	outfile << "<!-- Edges -->" << endl;
	int numStates = rows.size();
	for(int i = 0, count = 0; i < numStates; ++i, ++count)
	{
		vector<float> currRow = rows[i];
		int rowSize = currRow.size();
		for (int j = 0; j < rowSize; ++j)
		{
			if (currRow[j] != 0)
			{
				outfile << "<edge id=\"e" << i << j << "\" source=\"" << i << "\" target=\"" << j << "\">" << endl;
				outfile << "\t<data key=\"probability\">" << currRow[j] << "</data>" << endl;
				outfile << "</edge>" << endl;
			}
		}
	}
}

void PrintFooter(ofstream & outfile)
{
	outfile << "</graph>" << endl;
	outfile << "</graphml>" << endl;
}

void PrintToFile( vector<vector<float> > & rows,  vector<float> & eqProbs)
{
	ofstream outfile(ALPHA_OUTPUT);
	//outfile << scientific;
	
	//TODO: Print header, then keys,
	// then nodes, then edges, then close
	
	PrintHeader(outfile);
	PrintDataSchema(outfile, eqProbs);
	PrintNodes(outfile, rows.size(), eqProbs, 0);
	PrintEdges(outfile, rows);
	PrintFooter(outfile);
	
	outfile.close();
	
	
	//PrintCol pc(outfile);
	//for_each(rows.begin(), rows.end(), pc);
}


// Functor to convert strings to floats in insert those int 
// the column matrix
class ConvertAndInsert
{
public:
	explicit ConvertAndInsert(vector<float> & currCol): col(currCol)
	{
	}
	
	void operator() (string str)
	{
		float f = StringToFloat(str);
		col.push_back(f);
	}
private:
	vector<float> & col;
};

void ConvertDat (ifstream & infile, ifstream & epIn)
{
	cout << "Converting from tProb .dat-type format to GraphML..." << endl;
	vector<vector<float> > rows;
	
	while (true)
	{
		vector<float> currCol;
		string line;
		getline(infile, line);
		
		if (infile.eof()) break;
		
		stringstream sstream(line);
		string str;

		ConvertAndInsert cai(currCol);
		while (getline(sstream, str, ' '))
			cai(str);
			   
		rows.push_back(currCol);
	}
	
	infile.close();
	
	vector<float> eqProbs;
	copy(istream_iterator<float>(epIn), istream_iterator<float>(), back_inserter(eqProbs));
	
	epIn.close();
	
	PrintToFile(rows, eqProbs);
}

void ConvertMtx(ifstream & infile, ifstream & epIn)
{
	cout << "Converting from .mtx to GraphML..." << endl;
	vector<vector<float> > rows;
	
	vector<float> eqProbs;
	copy(istream_iterator<float>(epIn), istream_iterator<float>(), back_inserter(eqProbs));
	
	epIn.close();
	
	
	infile.ignore(256,'\n');
	infile.ignore(256,'\n');
	
	string line;
	
	getline(infile, line);
	stringstream getter(line);
	int numStates;
	getter >> numStates;
	
	ofstream outfile(ALPHA_OUTPUT);
	PrintHeader(outfile);
	PrintDataSchema(outfile, eqProbs);
	
	PrintNodes(outfile, numStates, eqProbs, 1);
	
	getline(infile, line);
	
	while (!infile.eof())
	{
		
		stringstream tokenizer(line);
		string source;
		string target;
		string probability;
		tokenizer >> source >> target >> probability;
		outfile << "<edge id=\"e" << source << target << "\" source=\"" << source << "\" target=\"" << target << "\">" << endl;
		outfile << "\t<data key=\"probability\">" << probability << "</data>" << endl;
		outfile << "</edge>" << endl;
		
		getline(infile, line);
	}
	
	PrintFooter(outfile);
	
	
	
}

void ConvertDot(ifstream & infile) //TODO: Decomp. Heh.
{
	cout << "Converting from Graphviz Dot to GraphML..." << endl;
	string line;
	getline(infile, line);
	getline(infile, line);
	
	ofstream outfile(ALPHA_OUTPUT);
	
	vector<float> newV;
	newV.clear();

	PrintHeader(outfile);
	PrintDataSchema(outfile, newV);
	outfile << "<key id=\"eqProb\" for=\"node\" attr.name=\"eqProb\" attr.type=\"double\"/>" << endl;
	outfile << "<key id=\"flux\" for =\"edge\" attr.name=\"flux\" attr.type=\"double\"/>" << endl;
	endl(outfile);
	
	//PrintDOTNodes
	outfile << "<!-- Nodes -->" << endl;
	while (line.find("->") == string::npos)
	{
		stringstream sstream(line);
		string nodeLabel;
		//string junk;
		string eqProb;
		sstream >> nodeLabel;
		sstream.ignore(256,'w');
		sstream >> eqProb;
		outfile << "<node id=\"" << nodeLabel << "\">" << endl;
		outfile << "\t<data key=\"label\">" << nodeLabel << "</data>" << endl;
		outfile << "\t<data key=\"eqProb\">" << eqProb.substr(6, eqProb.find_last_of("\"") - 6) << "</data>" << endl;
		outfile << "</node>" << endl;
		getline(infile, line);
	}
	
	//PrintDOTEdges
	while (line[0] != '}')
	{
		stringstream tokenizer(line);
		string source;
		string target;
		string arrow;
		string size;
		tokenizer >> source >> arrow >> target >> size;
		outfile << "<edge source=\"" << source << "\" target=\"" << target << "\">" << endl;
		outfile << "\t<data key=\"flux\">" << size.substr(12, size.length() - 14) << "</data>" << endl;
		outfile << "</edge>" << endl;
		getline(infile, line);
	}
	
	PrintFooter(outfile);
	
	outfile.close();
}


int main (int argc, char * const argv[]) 
{
	ifstream infile;
	ifstream epIn;
	string filename;
	string epFile;
	
	if (argc == 2 || argc == 3)												//If one argument is provided
	{
		if (argc == 2 && strstr(argv[1], ".dot") == NULL)
			GetEPStream(epIn);
		else if (argc == 3)
		{
			epFile = argv[2];
			epIn.open(epFile.c_str());
			if (epIn.fail())
			{
				cerr << "Error reading file " << argv[2] << endl;
				GetEPStream(epIn);
			}
		}
		
		filename = argv[1];
		infile.open(filename.c_str());							//Try to open the file (1st arg)
		if (infile.fail())
		{
			cerr << "Error reading file " << argv[1] << endl;
			GetEPStream(epIn);									//And if that fails
			GetStream(infile, filename);						//Ask for the file
		}
	} else  {													//If one argument is not supplied
		if (argc > 3)
			cerr << "Too many arguments supplied." << endl;
																//complain if >1 args were provided
		GetStream(infile, filename);							//Then ask the user for the file to convert
	}															//Note that we don't complain if no args are
																//provided; this is one expected use case.
	string filetype = filename.substr(filename.rfind('.'));
	if (filetype == ".dot")
		ConvertDot(infile);
	else if (filetype == ".mtx") {
		//GetEPStream(epIn);
		ConvertMtx(infile, epIn);
	} else {
		//GetEPStream(epIn);
		ConvertDat(infile, epIn);
	}
	
	infile.close();
	
	cout << "File written to " << ALPHA_OUTPUT << endl;
	
    return 0;
}
