

extern "C" {
#include <ctype.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>
}

using namespace std;

#include "cvc3/include/vc.h"
#include "theory_arith.h"
#include "theory_bitvector.h"
#include "theory_arith.h"

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
#include "CLP.h"

namespace dp {

lbool CLP::Interpolate(const std::vector<CVC3::Expr>& f, std::vector<CVC3::Expr>& result) {
	MSG(0,"CLP::Interpolate\n");

	for(size_t median = 1; median < f.size(); ++median) {

		//no need to compute trivial interpolant
		if(f[median-1]==lang::vc.trueExpr())
			continue;

		std::string query = "interpolate([";
		for(size_t i=0;i<median;++i) {
			if(i>0) query+=",";
			query += toCLPString(f[i]);
		}
		query +=" ],[ ";
		for(size_t i=median;i<f.size();++i) {

			if(i>median) query+=",";
			query += toCLPString(f[i]);
		}
		query +="]).";

		MSG(1,query+"\n");

		// pose the query
		WriteToCLP(query);
		// read answer
		vector<string> answer;
		ReadFromCLP(answer,1);
		for(size_t i=0;i<answer.size();++i)
			MSG(0,"CLP says:" + answer[i]+"\n");
	}


	return l_true;

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

CLP::CLP() {
	MSG(1,"CLP::CLP()\n");
	//create the name of cvs executable file

	if((pipe(toCLP) == -1) || (pipe(fromCLP) == -1)) {
		throw util::RuntimeError("ERROR: could not create file descriptors for communication ...\n");
	}

	//start the CLP process
	proc = fork();
	//failure
	if(proc == -1) throw util::RuntimeError("ERROR: could not fork for creating CLP process ...\n");
	//child process
	if(proc == 0) {
		//close file descriptors
		if((close(toCLP[1]) == -1) || (close(fromCLP[0]) == -1) ||
		(close(0) == -1) || (close(1) == -1)) {
		assert(false);
		}
		//duplicate file descriptors
		if((dup(toCLP[0]) == -1) || (dup(fromCLP[1]) == -1)) {
		assert(false);
		}
		//exec the CLP process
		if(execlp("clp-prover","clp-prover",(char*) NULL) == -1) {
		assert(false);
		}
	}
	//parent process
	else {
		MSG(1,"CLP process started ...\n");
		//close file descriptors
		if((close(toCLP[0]) == -1) || (close(fromCLP[1]) == -1)) {
			throw util::RuntimeError("ERROR: could not close pipe file descriptors in parent ...\n");
		}
		//convert file descriptors into FILE*
		if(((out = fdopen(toCLP[1],"w")) == NULL) || ((in = fdopen(fromCLP[0],"r")) == NULL)) {
			throw util::RuntimeError("ERROR: could not convert file descriptors to FILE* in parent ...\n");
		}
		//reset data structures
		totalQuerySize = 0;
		//read up the initial CLP prompt
		vector<string> intro;
		ReadFromCLP(intro,3);
		/*for(size_t i=0;i<intro.size();++i)
			MSG(0,"CLP says:" + intro[i]+"\n");
		*///setup the environment - load the axioms etc.
		SetupEnv();
	}
}

void CLP::ReadFromCLP(vector<std::string>& result, int limit) {
	bool outer_loop = true;
	int line_count = 0;
	while (outer_loop)
	{
		if(line_count >= limit) break;

 		string line;
		bool inner_loop = true;
 		while(inner_loop) {

 			char a = static_cast<char> (fgetc(in));
			switch(a) {
				case '^':
				case EOF:
					outer_loop = inner_loop = false;
					break;
				case '\n':
					inner_loop = false;
					++line_count;
					break;
				default:
					line += a;
					break;
			}
 		}
		if(line!="")
		result.push_back(line);
	}
}


void CLP::WriteToCLP(const std::string &line) {
  	fprintf(out,"%s\n",line.c_str());
  	fflush(out);
}

void CLP::SetupEnv()
{
}

//shutdown the theorem prover
void CLP::Shutdown() {

	WriteToCLP("\n");
	fflush(stdout);
	close(toCLP[1]);
	close(fromCLP[0]);

	//destroy the CLP process
	if(kill(proc,SIGKILL) == -1) {
	throw util::RuntimeError("ERROR: could not destroy CLP process ...\n");
	} else {
	if(waitpid(proc,NULL,0) != proc) {
	throw util::RuntimeError("ERROR: could not free resources of CLP process ...\n");
	} else {
	MSG(1,"CLP process destroyed ...\n");
	}
	}
}


std::string CLP::toCLPString(const CVC3::Expr& e) {
	std::string result;

	std::vector<CVC3::Expr> conjuncts;

	lang::ExprManager::getTopLevelConjuncts(e,conjuncts);

	for(size_t i = 0; i<conjuncts.size();++i) {
		if(i>0) result +=",";
		result += toCLPStringRec(conjuncts[i]);
	}
	return result;
}

std::string CLP::toCLPString(const vector<CVC3::Expr>& ev) {
	std::string result;

	std::vector<CVC3::Expr> conjuncts;

	for(size_t i = 0; i<ev.size(); ++i) {
		lang::ExprManager::getTopLevelConjuncts(ev[i],conjuncts);
	}

	result = "[";

	int counter = 0;
	for(size_t i = 0; i<conjuncts.size();++i) {
		string conjunct_string (toCLPStringRec(conjuncts[i]));
		if(conjunct_string=="1=1") continue;
		if(counter>0) result +=",";
		result += conjunct_string;
		++counter;
	}
	result +="]";
	return result;
}

std::string CLP::toCLPStringRec(const CVC3::Expr& e) {
	std::string result;

	std::string skids[e.arity()];


	const std::vector< CVC3::Expr > & kids = e.getKids();

	/* start with child nodes */
	if(e.arity()>0) {
		for(unsigned i = 0; i<kids.size(); ++i) {
			std::string s (toCLPStringRec(kids[i]));
			skids[i] = kids[i].arity()>1 ? "("+ s +")" : s;

		}
	}

	switch(e.getKind()) {
		case CVC3::TRUE_EXPR:
			result = "1=1";
			break;
		case CVC3::FALSE_EXPR:
			result = "0=1";
			break;
		case CVC3::ID:
		case CVC3::UCONST:
		{
			switch(e.getType().getExpr().getKind()) {
				case CVC3::BITVECTOR: {
					throw util::RuntimeError("bitvectors not supported\n");
					}
					break;
				case CVC3::INT:
				case CVC3::REAL:
				case CVC3::BOOLEAN: {
					result = e.toString();
					break;
				}
				default: break;
			}
			break;
		}
		/* Let requires extra treatment as it binds variables
		   in subexpressions. These variables have to be handled
		   either by replacement or by introducing them into the
		   translation table. Out of laziness, we do the former. */
		case CVC3::LET:
			cout<<"LET expression"<<endl;

			break;
		case CVC3::RATIONAL_EXPR:
			{
				result = e.getRational().toString();
			}
			break;
		case CVC3::ITE:
			result = "ite (" + skids[0] + "," + skids[1] + "," + skids[2]  + ")";
			break;
		case CVC3::NOT:
			throw RuntimeError("CLP: Negation is not supported: " + e.toString() + "\n");
			result = "!" + skids[0];
			break;
		case CVC3::AND:
			result = "[" + skids[0] + "," + skids[1] + "]";
			break;
		case CVC3::OR:
			throw RuntimeError("CLP: OR is not supported: " + e.toString() + "\n");
			result = skids[0] + "+" + skids[1];
			break;
		case CVC3::XOR:
			throw RuntimeError("CLP: XOR is not supported\n");
			result = skids[0] + "#" + skids[1];
			break;
		case CVC3::IMPLIES:
			throw RuntimeError("CLP: IMPLIES is not supported\n");
			result = skids[0] + "=<" + skids[1];
			break;
		case CVC3::IFF:
			throw RuntimeError("CLP: IFF is not supported\n");
			result = skids[0] + "=" + skids[1];
			break;
		case CVC3::EQ:
			result = skids[0] + "=" + skids[1];
			break;
		case CVC3::NEQ:
			throw RuntimeError("CLP: DISEQ is not supported\n");
			result = skids[0] + "#" + skids[1];
			break;

		/* arithmetic */
		case CVC3::LT:
			result = skids[0] + "<" + skids[1];
			break;
		case CVC3::GT:
			result = skids[0] + ">" + skids[1];
			break;
		case CVC3::LE:
			result = skids[0] + "=<" + skids[1];
			break;
		case CVC3::GE:
			result = skids[0] + ">=" + skids[1];
			break;
		case CVC3::PLUS:
			result = skids[0] + "+" + skids[1];
			break;
		case CVC3::MINUS:
			result = skids[0] + "-" + skids[1];
			break;
		case CVC3::UMINUS: {
			result = "(- "+skids[0]+")";
			}
			break;
		case CVC3::MULT:
			result = skids[0] + "*" + skids[1];
			break;
		case CVC3::DIVIDE:
			result = skids[0] + "/" + skids[1];
			break;
		case CVC3::MOD:
			result = skids[0] + "%" + skids[1];
			break;

		/* bitvector operations */
		case CVC3::BVCONST:
		case CVC3::CONCAT:
		case CVC3::EXTRACT:
		case CVC3::BOOLEXTRACT:
		case CVC3::LEFTSHIFT:
		case CVC3::CONST_WIDTH_LEFTSHIFT:
		case CVC3::RIGHTSHIFT:
		case CVC3::BVSHL:
		case CVC3::BVLSHR:
		case CVC3::BVASHR:
		case CVC3::SX:
		case CVC3::BVREPEAT:
		case CVC3::BVZEROEXTEND:
		case CVC3::BVROTL:
		case CVC3::BVROTR:
		case CVC3::BVAND:
		case CVC3::BVOR:
		case CVC3::BVXOR:
		case CVC3::BVXNOR:
		case CVC3::BVNEG:
		case CVC3::BVNAND:
		case CVC3::BVNOR:
		case CVC3::BVCOMP:
		case CVC3::BVUMINUS:
		case CVC3::BVPLUS:
		case CVC3::BVSUB:
		case CVC3::BVMULT:
		case CVC3::BVUDIV:
		case CVC3::BVSDIV:
		case CVC3::BVUREM:
		case CVC3::BVSREM:
		case CVC3::BVSMOD:
		case CVC3::BVLT:
		case CVC3::BVLE:
		case CVC3::BVGT:
		case CVC3::BVGE:
		case CVC3::BVSLT:
		case CVC3::BVSLE:
		case CVC3::BVSGT:
		case CVC3::BVSGE:
		case CVC3::INTTOBV:
		case CVC3::BVTOINT:
		default:
			MSG(0,"unsupported expression "+lang::vc.getEM()->getKindName(e.getKind()));
			break;



			break;
	}
	return result;
}



CLP::~CLP() {
	Shutdown();

}

}
