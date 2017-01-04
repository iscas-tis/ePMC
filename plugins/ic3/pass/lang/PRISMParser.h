#ifndef __PRISM_PARSER
#define __PRISM_PARSER

#include "AST.h"

namespace lang {
  class Model;
}

namespace PRISM {

class PRISMParser {
public:

	PRISMParser();
	~PRISMParser();
	void run(const std::string& file, lang::Model &);
	static AST::Model astModel;
private:
	void* table;
};

// flex- and bison-specific stuff
extern int line_number;

extern AST::Substitution constants;

}

#endif
