#include <csignal>

#include "util/Util.h"
#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include "util/Cube.h"
#include <fstream>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "pred/Predicate.h"
#include "pred/PredSet.h"
#include "lang/Model.h"
#include "bdd/BDD.h"
#include "bdd/ODD.h"
#include "dp/SMT.h"
#include "dp/YicesSMT.h"

#include "model_checker/GameGraph.h"
#include "model_checker/MDPSparse.h"
#include "model_checker/Lazy.h"
#include "pred/EncodingManager.h"
#include "pred/Cluster.h"
#include "pred/AbsModel.h"
#include "pred/AbsModelImpl.h"

using namespace util;

/* parsing */
void yyparse();


std::string model_name;
lang::Model model;
pred::AbsModel* abs_model;


/*! \brief make tool ready to start */
void Init(std::vector<std::string>& argvec) {
	//ios_base::sync_with_stdio(false);
	lang::ExprManager::Init();
	util::Statistics::globalTimer.Start();
	Database::ProcessCommandLineArguments(argvec);

	Database::parseInputs(model);

    if (model.getProperties().size() == 0)
    {
    	  MSG(0,"There is no property to check!\n");
	      exit(1);

    }

	if (!model.usesOnlyConstantRates()) {
	  MSG(0,"At the moment only constant rates are supported\n");
      exit(1);

	}
	model.Flatten();

	if (lang::CTMC == model.getModelType()) {
	  model.CTMC2CTMDP();
	  std::cout << model.toString() << std::endl;
	  std::cout << "DONE ENUM" << std::endl;
	}
	if( Database::PrettyPrintModel ) {
		std::ofstream m_pretty("out.pretty");
		m_pretty<<model.toString();
	}
}

/*! \brief clean up and terminate the tool */
void Done() {
	lang::ExprManager::Done();
	util::Statistics::globalTimer.Stop();
	util::Statistics::Display();
}

void Abort ( int sig) {
	MSG(0,"PASS has been aborted\n");
	util::Statistics::globalTimer.Stop();


	util::Statistics::Display();
	exit(1);
}


/*! \brief build and check abstract model w/o CEGAR */
void AbstractAndCheck() {
	abs_model = new pred::AbsModelImpl(model,dp::SMT::getSMT());
	abs_model->InitialExtract();
	MSG(1,"Abstract Model built\n");
	if(Database::CheckProperties)
  		abs_model->CheckProperties();

}

/*! \brief build abstract model from concrete model */
int CEGAR() {
	abs_model = new pred::AbsModelImpl(model,dp::SMT::getSMT());
	return abs_model->CEGAR();
}


/*! \brief main function */
int main(int argc, char *argv[]) {

	// if Ctrl-C is pressed exit gracefully and give statistics
	(void) signal (SIGINT,Abort);	

	int return_code = 0;
	std::vector<std::string> argvec(argc);
	for(int i=1; i<argc; ++i) {
		argvec[i] = argv[i];
	}
	try {
		Init(argvec);
		if(Database::CEGAR)
			return_code = CEGAR();
		else
			AbstractAndCheck();
		Done();
	} catch( RuntimeError& e) {
		MSG(0,e.toString() + "\n");
	} catch ( CVC3::Exception& e) {
		// MSG(0,"CVC3 exception: " + e.toString() + " \n");
	}

	return return_code;
};

