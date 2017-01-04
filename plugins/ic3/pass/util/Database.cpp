/******************************** CPPFile *****************************

* FileName [Database.cpp]

* PackageName [util]

* Synopsis [This file contains procedures to access and manipulate
* global data structures.]

* SeeAlso [Database.h]

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2007 by Saarland University. All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

#include "vcl.h"

#include "Database.h"
#include "Util.h"
#include "lang/SymbolTable.h"
#include "lang/AST.h"
#include "lang/PRISMParser.h"

#include "Error.h"
#include "dp/SMT.h"

int line_number;       // current line number
std::string file_name; // name of the file currently being parsed

using namespace std;

namespace util {

namespace Database {
bool DEBUG                       = false;
bool displayMarkovChain		 = false;

int VERBOSITY_LEVEL              = 0;
int USE_SYNTACTIC_CHECKS         = 1;
int VALID_PTR_PRED               = 0;
int IGNORE_POINTERS              = 1;
#ifdef WITH_ADVANCED_SMT
TP THEOREM_PROVER_USED = Yices;
#else
TP THEOREM_PROVER_USED = CVC3;
#endif
IP INTERPOLATION = MathSat;

bool FACTORIZE_INTERPOLATION = false;

bool DUMP_PROOF_SCRIPT           = false;
bool SMT_TYPE_CHECK              = false;


Strategy strategy = optDiff;
bool ProbEager = false;

int DISPLAY_STATISTICS           = 1;
int INT_TYPE;
int FLOAT_TYPE;
int BOOL_TYPE;
std::string model_name;
std::string model_file_name;

MCTechniques MCTechnique = sparse;

ValiterOrder valiter_order = rdfs;


bool PrettyPrintModel = false;
bool CheckProperties = true;
bool MCPreprocessing = true;
bool MCComputeSchedule = false;

bool CEGAR = true;
bool displayCEX = false;
unsigned CEGARStrategy = 3;
unsigned CEGARMaxSpurious = 3;



bool CartesianAbstraction = false;

bool CompactProbChoices = false;

unsigned CEMaxNrOfPaths = 0;

double epsilon = 1E-04;
double term_crit_param = 1E-06;

int term_crit = 3;
int max_iters = 10000;
bool extend_yes = true;

unsigned COIBound = 10;

bool PredsFromInit = false;
bool PredsFromGuards = false;

bool Split = false;

bool incremental = true;


HashMap<std::string,bool> BoolOptionValue;
HashMap<std::string,std::string> OptionValue;


char const * ValidOptions [] = {
                         "help", "Debug","Verbose", "Preprocessing","PrettyPrint", "DumpProofScript", 
                         "FIP", "CLP", "Split", "CEMaxNrOfPaths", "epsilon",  "CEGARStrategy", "CEGARMaxSpurious",
                         "term_crit", "max_iters", "COIBound", "ProbEager", 0
                         };


void AddBoolOption(const string& opt, bool defaultvalue)
/*******************************************************
 * <AddBoolOption> creates new option with name <opt>
 * and default value <defaultvalue>
 * ******************************************************/
{
  BoolOptionValue[opt] = defaultvalue;
}

bool IsOptionSet(const std::string& opt) {
	HashMap<std::string,bool>::const_iterator i = BoolOptionValue.find(opt);
	if(i!=BoolOptionValue.end())
		return i->second;
	else
		return false;
}

void AddOption(const std::string& opt, const std::string& defaultvalue)
/*******************************************************
 * <AddOption> creates new option with name <opt>
 * and default value <defaultvalue>
 * ******************************************************/
{
  OptionValue[opt] = defaultvalue;
}

std::string getOption(const char* opt) {
	HashMap<std::string,std::string>::const_iterator i = OptionValue.find(opt);
	if(i!=OptionValue.end())
		return i->second;
	else
		return "NOT SET";
}

bool IsValueOfOptionEqualTo(const std::string& opt, const std::string& value) {
	HashMap<std::string,std::string>::const_iterator i = OptionValue.find(opt);
	if(i!=OptionValue.end())
		return i->second == value;
	else
		return false;
}

void PrintOptions() {
	MSG(1,"Argument value: \n");
	MSG(1,"----------------------------\n");
	for(HashMap<std::string,bool>::const_iterator i = BoolOptionValue.begin(); i != BoolOptionValue.end(); ++i)
		MSG(1,(i->first) + ": " + (i->second ? "true" : "false") + "\n");

	for(HashMap<std::string,std::string>::const_iterator i = OptionValue.begin(); i != OptionValue.end(); ++i)
		MSG(1,(i->first) + ": " + (i->second) + "\n");
}

/*! \brief remove file extension from file name */
std::string RemoveExtension(std::string s) {
	if(s.size()==0) return "";
	size_t pos = s.find_last_of('.');
	if(pos!=std::string::npos)
		return std::string(s.begin(), s.begin() + pos);
	else
		return s;
}

vector<string > files;

void parseInputs(lang::Model &model) {

	PRISM::PRISMParser p;
	for(unsigned i=0; i<files.size();++i) {
		file_name = files[i];
		line_number = 1;

		try {
            p.run(file_name,model);
		} catch ( ParseError& e) {
			std::cout<<e<<endl;
			exit(10);
		} catch ( TypeError& e) {
			std::cout<<e<<endl;
			exit(10);
		} catch ( RuntimeError& e) {
			std::cout<<e<<endl;
			exit(10);
		}
		fclose(stdin);
	}
}






const std::string Usage =
"______________________________________________\n"\
"pass [parameters] model.pass [properties.pctl]\n"\
"\n"\
"* operation:                                  \n"\
"  PASS reads in model.pass and properties.pctl\n"\
"  and creates model.pm and model.pctl         \n"\
"\n"\
"* parameters:                                 \n"\
" -v ... verbose                               \n"\
" -l ... have SMT solver log its operations    \n"\
"______________________________________________\n";
;

bool have_input = false;


bool IsValidOption(std::string arg)
{
    int i = 0;
    for(i=0; ValidOptions[i] && strcmp(ValidOptions[i++], arg.c_str()); /*nothing*/);
    return ValidOptions[i] != 0;
}

void parseArg( std::string arg )
{
    bool need_help = false;
	/* if has a '=' sign get value */
	size_type i = arg.find('=');

	if( i!= std::string::npos){ /* it is an option value pair */
		std::string key = arg.substr(0,i);
		std::string value = arg.substr(i+1,arg.size());
		OptionValue[key] = value;
		
		if(!IsValidOption(key))
		{
		    need_help = true;
		}
	} else { /* regular options with no '=' sign  */
		BoolOptionValue[arg] = true;
		if (!IsValidOption(arg))
		{
		    need_help = true;
		}
	}
	
	if (need_help)
	{
	    throw RuntimeError("Invalid option: --" + arg + "\nTry pass --help for help.");
	}
}


/*! \brief parse model and property files and interpret command line args */
void ProcessCommandLineArguments(const std::vector<string>& argv) {
	AddBoolOption("Debug",false);
	AddBoolOption("Verbose",false);
	AddBoolOption("Preprocessing", true);
	AddBoolOption("PrettyPrint", false);
	AddBoolOption("DumpProofScript", false);
	AddOption("FIP", "1");
	AddBoolOption("CLP",false);
	AddBoolOption("Split",false);
	AddOption("CEMaxNrOfPaths","100");
	AddOption("epsilon","1E-04");
	AddOption("CEGARStrategy","3");
	AddOption("CEGARMaxSpurious","1");
    	AddOption("term_crit","3");
	AddOption("max_iters","10000");
	AddOption("COIBound","10");
	AddBoolOption("ProbEager",false);
	AddBoolOption("help", false);


	for (size_type i=1; i<argv.size(); ++i) {
		const string arg = argv[i];

		if( arg.size() > 2 && arg.find("--")==0)
		{ /* long option */
			parseArg( arg.substr(2,arg.size()));
		}
		else if (arg.find("-") == 0)
		{
		    throw RuntimeError("Invalid option: " + arg + "\nTry pass --help for help.");
		}
        else {
			files.push_back(arg);
			if(!have_input) {
				model_name = RemoveExtension(arg);
				model_file_name = model_name + ".pm";
			}
			have_input = true;
		}
	} //end of for

	CEMaxNrOfPaths = util::stringToInt(getOption("CEMaxNrOfPaths"));
	epsilon = util::stringToDouble(getOption("epsilon"));
	term_crit_param = util::stringToDouble(getOption("term_crit_param"));    	
	term_crit = util::stringToInt(getOption("term_crit"));
	max_iters = util::stringToInt(getOption("max_iters"));
	extend_yes = IsOptionSet("extend_yes");
	ProbEager = IsOptionSet("probeager");

	Split = IsOptionSet("Split");


	COIBound = util::stringToInt(getOption("COIBound"));

	if(IsValueOfOptionEqualTo("Interpolator", "MathSat")) {
		INTERPOLATION = MathSat;
	} else 	if(IsValueOfOptionEqualTo("Interpolator", "CLP")) {
		INTERPOLATION = CLP;
	} else if(IsValueOfOptionEqualTo("Interpolator", "CSIsat")) {
		INTERPOLATION = CSIsat;
	} else {
		INTERPOLATION = MathSat;
	}

	if(IsValueOfOptionEqualTo("valiter_order", "rdfs")) {
		valiter_order = rdfs;
	} else if(IsValueOfOptionEqualTo("valiter_order", "random")) {
		valiter_order = random;
	} else if(IsValueOfOptionEqualTo("valiter_order", "topo")) {
		valiter_order = topo;
	}

	if(IsValueOfOptionEqualTo("strategy", "optDiff")) {
		strategy = optDiff;
	} else if(IsValueOfOptionEqualTo("strategy", "optDiffPath")) {
		strategy = optDiffPath;
	} else if (IsValueOfOptionEqualTo("strategy", "anyDiff")) {
		strategy = anyDiff;
	} else if(IsValueOfOptionEqualTo("strategy", "strongestEvidence")) {
		strategy = strongestEvidence;
	} else if(IsValueOfOptionEqualTo("strategy", "onlyScheduler")) {
		strategy = onlyScheduler;
	}

	if(IsValueOfOptionEqualTo("TP", "Yices")) {
		THEOREM_PROVER_USED = Yices;
	} else if (IsValueOfOptionEqualTo("TP", "YicesLight")) {
		THEOREM_PROVER_USED = YicesLight;
	} else if(IsValueOfOptionEqualTo("TP", "CVC3")) {
		THEOREM_PROVER_USED = CVC3;
	}
	// falling back to default
	else {
#ifdef WITH_YICES
		THEOREM_PROVER_USED = Yices;
#else
		THEOREM_PROVER_USED = CVC3;
#endif
	}


	PredsFromInit = IsOptionSet("PredsFromInit");
	PredsFromGuards = true; // fixme: IsOptionSet("PredsFromGuards");

	if(IsValueOfOptionEqualTo("engine", "sparse")) {
		MCTechnique = sparse;
	} else if (IsValueOfOptionEqualTo("engine", "mtbdd")) {
		MCTechnique = mtbdd;
	} else if (IsValueOfOptionEqualTo("engine", "hybrid")) {
		MCTechnique = hybrid;
	} else if (IsValueOfOptionEqualTo("engine", "game")) {
		PredsFromGuards = false;
		MCTechnique = game;
	}

	DEBUG = IsOptionSet("Debug");
	FACTORIZE_INTERPOLATION = util::stringToInt(getOption(("FIP")));;
	VERBOSITY_LEVEL = IsOptionSet("Verbose");
	PrettyPrintModel = IsOptionSet("PrettyPrint");
	DUMP_PROOF_SCRIPT = IsOptionSet("DumpProofScript");
	DEBUG = IsOptionSet("Debug");
	MCPreprocessing = IsOptionSet("Preprocessing");
	CompactProbChoices = IsOptionSet("CompactProbChoices");
	displayCEX = IsOptionSet("displayCEX");
	displayMarkovChain = IsOptionSet("displayMarkovChain");

	CEGARStrategy = util::stringToInt(getOption(("CEGARStrategy")));
	CEGARMaxSpurious = util::stringToInt(getOption(("CEGARMaxSpurious")));

	CartesianAbstraction = IsOptionSet("CartesianAbstraction");



	if(IsOptionSet("NoCEGAR")) {
		CEGAR = false;
	}
	
    if (IsOptionSet("help"))
    {
        MSG(0, Usage);
        exit(0);
    }

	if(!have_input) {
		throw RuntimeError("no input files");
	}

	PrintOptions();
    

	BoolOptionValue.clear();
	OptionValue.clear();
}










}

} //end of util

/*********************************************************************/
//end of Database.cpp
/*********************************************************************/
