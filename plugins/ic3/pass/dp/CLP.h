#ifndef CLP_H_
#define CLP_H_

namespace dp {

class CLP : public SMT {
	public:
	//! invoke the CLP-Prover process
	CLP();
	//! kill the CLP-Prover process
	virtual ~CLP();

	/*! \brief compute interpolant
	*/
  	virtual lbool Interpolate(const std::vector<CVC3::Expr>& f, std::vector<CVC3::Expr>& result);
private:

	//!create the file descriptors for communication
	int toCLP[2],fromCLP[2];
	
	//!the simplify process id
  	pid_t proc;
  	//!the output stream piped to stdin of the process
  	FILE *out;
  	//!the input stream piped to the stdout of the process
  	FILE *in;

	//!the total size of all the queries made so far
  	size_t totalQuerySize;

	void SetupEnv();

	void Shutdown();
	void ReadFromCLP(vector<std::string>& result, int limit=0);
	void WriteToCLP(const std::string &line);

	std::string toCLPString(const CVC3::Expr& e);
	std::string toCLPString(const std::vector<CVC3::Expr>& e);
	std::string toCLPStringRec(const CVC3::Expr& e);
};

}

#endif
