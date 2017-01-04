extern "C" {
#include <ctype.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>
}

using namespace std;


#include "theory_arith.h"
#include "theory_bitvector.h"
#include "theory_arith.h"

#include "lang/Node.h"
#include "util/Util.h"
#include "util/Cube.h"
#include "lang/SymbolTable.h"
#include "lang/ExprManager.h"

#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"

using namespace util;

#include "SMT.h"
#include "YicesSMT.h"
#include "CVC3SMT.h"

#include "FOCIParser.h"
#include "CSIsat.h"

using namespace lang;

namespace dp {

std::string CSIsat::toCSIsatStringFlatten(const CVC3::Expr& f) {
	vector<CVC3::Expr> flattened;
	lang::ExprManager::getTopLevelConjuncts(f,flattened);
	return toCSIsatString(flattened);
}


lbool CSIsat::Interpolate(const std::vector<CVC3::Expr>& f, std::vector<CVC3::Expr>& result) {
	MSG(1,"CSIsat::Interpolate\n");
	std::string query;
	query += toCSIsatStringFlatten(f[0]);
	for(size_t i=1;i<f.size();++i) {
		if(f[i]==lang::vc.trueExpr()) continue;
		query += "; "+toCSIsatStringFlatten(f[i]);
	}

	MSG(1,"CSIsat: " + query + " ?\n");

	//invokeCSIsat();
	WriteToCSIsat(query);
	std::string csi_result = ReadFromCSIsat();
	//terminateCSIsat();

	MSG(1,"CSIsat: " + csi_result + " !\n");

	if(csi_result.find("Satisfiable")!=std::string::npos) {
		MSG(1,"SAT \""+query+"\"\n");
		return l_false;
	} else if (csi_result.find("Fatal")!=std::string::npos) {
		MSG(0,"syntax error in \""+query+"\": \"" + csi_result + "\" \n");
		return l_false;
	} else {
		FOCIParser parser(symbol_table);
		parser.parseSequence(csi_result,result);
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

/*! get the expression as a string that the CSIsat interpolating theorem prover understands */
std::string CSIsat::toCSIsatString(const vector<CVC3::Expr>& v) {
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



CSIsat::CSIsat() {
	MSG(1,"CSIsat::CSIsat()\n");
	invokeCSIsat();
}

void CSIsat::invokeCSIsat() {
	MSG(1,"CSIsat::CSIsat()\n");

	if((pipe(toCSIsat) == -1) || (pipe(fromCSIsat) == -1)) {
		throw util::RuntimeError("ERROR: could not create file descriptors for communication ...\n");
	}

	int in_read = fromCSIsat[0];
	int in_write = fromCSIsat[1];
	int out_read = toCSIsat[0];
	int out_write = toCSIsat[1];

	//start the CSIsat process
	proc = fork();
	//failure
	if(proc == -1) throw util::RuntimeError("ERROR: could not fork for creating CSIsat process ...\n");
	//child process
	if(proc == 0) {

		int input = out_read;
		int output = in_write;

		int result;
		result = dup2(input,0);
		assert(result!=-1);
		result = close(input);
		assert(result!=-1);
		result = dup2(output,1);
		assert(result!=-1);
		result = close(output);
		assert(result!=-1);
		//exec the CSIsat process
		result = execlp("csisatServer","csisatServer","-round","-int",(char*) NULL);
		assert(result!=-1);
	}
	//parent process
	else {
		MSG(1,"CSIsat process started ...\n");
		//close file descriptors
		if((close(out_read) == -1) || (close(in_write) == -1)) {
			throw util::RuntimeError("ERROR: could not close pipe file descriptors in parent ...\n");
		}
		//convert file descriptors into FILE*
		if( ( out = fdopen(out_write,"w")) == NULL || ((in = fdopen(in_read,"r")) == NULL)) {
			throw util::RuntimeError("ERROR: could not convert file descriptors to FILE* in parent ...\n");
		}
		//reset data structures
		totalQuerySize = 0;
	}
}

std::string CSIsat::ReadFromCSIsat() {
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


void CSIsat::WriteToCSIsat(const std::string &line) {
  	fprintf(out,"%s\n",line.c_str());
  	fflush(out);
}



//shutdown the theorem prover
void CSIsat::terminateCSIsat() {
	WriteToCSIsat("q\n");
	fflush(stdout);
	close(toCSIsat[1]);
	close(fromCSIsat[0]);

	//destroy the CSIsat process
	if(kill(proc,SIGKILL) == -1) {
		throw util::RuntimeError("ERROR: could not destroy CSIsat process ...\n");
	} else {
		if(waitpid(proc,NULL,0) != proc) {
			throw util::RuntimeError("ERROR: could not free resources of CSIsat process ...\n");
		} else {
			MSG(1,"CSIsat process destroyed ...\n");
		}
	}
}


CSIsat::~CSIsat() {
	terminateCSIsat();
}


}
