#include <cstdlib>
#include <csignal>


extern "C" {
#include <ctype.h>
#include <sys/types.h>
#include <sys/wait.h>
}

using namespace std;

#include "theory_arith.h"
#include "theory_bitvector.h"
#include "theory_arith.h"

#include "lang/Node.h"
#include "util/Util.h"
#include "util/Cube.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"

#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"

using namespace util;

#include "SMT.h"
#include "YicesSMT.h"
#include "CVC3SMT.h"
#include "FOCIParser.h"
#include "FOCI.h"

using namespace lang;

namespace dp {

std::string FOCI::toFOCIStringFlatten(const CVC3::Expr& f) {
	vector<CVC3::Expr> flattened;
	lang::ExprManager::getTopLevelConjuncts(f,flattened);
	return toFOCIString(flattened);
}


lbool FOCI::Interpolate(const std::vector<CVC3::Expr>& f, std::vector<CVC3::Expr>& result) {

	std::string query = "n ";
	query += toFOCIStringFlatten(f[0]);


	for(size_t i=1;i<f.size();++i) {
		if(f[i].isTrue()) continue;
		query += "; "+toFOCIStringFlatten(f[i]);
	}

	WriteToFOCI(query);
	FOCIParser foci_parser(symbol_table);
 	MSG(1,"FOCI::Interpolate: \"" + query + "\"\n");
	std::string foci_result = ReadFromFOCI();

	MSG(1," result " + foci_result + " \n");
	if(foci_result.find("Satisfiable")!=std::string::npos) {
		MSG(1,"SAT \""+query+"\"\n");
		return l_false;
	} else if (foci_result.find("syntax")!=std::string::npos) {
		MSG(0,"syntax error in \""+query+"\"\n");
		return l_false;
	} else {
		foci_parser.parseSequence(foci_result,result);
		return l_true;
	}

}

/*
formula :: propositional_variable
         | '=' term term
         | '<=' term term
         | '&' '[' formula ... formula ']'
         | '|' '[' formula ... formula ']'
         | '~' formula

term :: individual_variable
         | '+' '[' term ... term ']'
         | '*' number term
         | uninterpreted_function_symbol '[' term ... term ']'
*/

/*! get the expression as a string that the FOCI interpolating theorem prover understands */
std::string FOCI::toFOCIString(const vector<CVC3::Expr>& v) {
	std::string result;
	if(v.size()==0) {
		result = "true";
	}
	else if(v.size()==1) {
		result = ExprManager::toFOCIString(v[0],symbol_table);
	}
	else {
		result += "& [ ";
		for(size_t i=0;i<v.size();++i) {
			result += ExprManager::toFOCIString(v[i],symbol_table) + " ";
		}
		result += " ]";
	}
	return result;
}


FOCI::FOCI() {
	MSG(1,"FOCI::FOCI()\n");

	if((pipe(toFOCI) == -1) || (pipe(fromFOCI) == -1)) {
		throw util::RuntimeError("ERROR: could not create file descriptors for communication ...\n");
	}

	//start the FOCI process
	proc = fork();
	//failure
	if(proc == -1) throw util::RuntimeError("ERROR: could not fork for creating FOCI process ...\n");
	//child process
	if(proc == 0) {
		//close file descriptors
		if((close(toFOCI[1]) == -1) || (close(fromFOCI[0]) == -1) ||
		(close(0) == -1) || (close(1) == -1)) {
		assert(false);
		}
		//duplicate file descriptors
		if((dup(toFOCI[0]) == -1) || (dup(fromFOCI[1]) == -1)) {
			assert(false);
		}
		//exec the FOCI process
		if(execlp("fociServer","fociServer",(char*) NULL) == -1) {
		assert(false);
		}
	}
	//parent process
	else {
		MSG(1,"FOCI process started ...\n");
		//close file descriptors
		if((close(toFOCI[0]) == -1) || (close(fromFOCI[1]) == -1)) {
			throw util::RuntimeError("ERROR: could not close pipe file descriptors in parent ...\n");
		}
		//convert file descriptors into FILE*
		if(((out = fdopen(toFOCI[1],"w")) == NULL) || ((in = fdopen(fromFOCI[0],"r")) == NULL)) {
			throw util::RuntimeError("ERROR: could not convert file descriptors to FILE* in parent ...\n");
		}
		//reset data structures
		totalQuerySize = 0;
		//read up the initial FOCI prompt
		std::string intro = ReadFromFOCI();
//		MSG(0,"FOCI says:" + intro + "\n");
		//setup the environment - load the axioms etc.
	}
}

std::string FOCI::ReadFromFOCI(int limit) {
	std::string result;
	bool loop = true;
	while (loop)
	{
		char a = static_cast<char> (fgetc(in));
		switch(a) {
			case '^':
			case EOF:
				loop = false;
				break;
			case '\n':
				result +=';';
				break;
			default:
				result += a;
				break;
 		}
	}
	return result;
}


void FOCI::WriteToFOCI(const std::string &line) {
  	fprintf(out,"%s\n",line.c_str());
  	fflush(out);
}

//shutdown the theorem prover
void FOCI::Shutdown() {

	WriteToFOCI("e\n");
	fflush(stdout);
	close(toFOCI[1]);
	close(fromFOCI[0]);

	//destroy the FOCI process
	if(kill(proc,SIGKILL) == -1) {
		throw util::RuntimeError("ERROR: could not destroy FOCI process ...\n");
	} else {
		if(waitpid(proc,NULL,0) != proc) {
			throw util::RuntimeError("ERROR: could not free resources of FOCI process ...\n");
		} else {
			MSG(1,"FOCI process destroyed ...\n");
		}
	}
}


void FOCI::getTopLevelConjuncts(const CVC3::Expr& e, std::vector<CVC3::Expr>& result) {
	switch(e.getKind()) {
		case CVC3::AND: {
			const std::vector< CVC3::Expr > & kids = e.getKids();
			getTopLevelConjuncts(kids[0],result);
			getTopLevelConjuncts(kids[1],result);
			break;
		}
		default: {
			result.push_back(e);
			break;
		}
	 }
}

FOCI::~FOCI() {
	Shutdown();

}

}
