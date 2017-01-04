#ifndef FOCI_H_
#define FOCI_H_

namespace dp {

class FOCI : public SMT {
	public:
	//! invoke the FOCI-Prover process
	FOCI();
	//! kill the FOCI-Prover process
	virtual ~FOCI();

	/*! get the expression vector (conjunction) as a string that FOCI understands */
	std::string toFOCIString(const vector<CVC3::Expr>& v);

	/*! get the expression as a string that FOCI understands */
	std::string toFOCIString(const CVC3::Expr& e);

	/*! \brief compute interpolant
	*/
  	virtual lbool Interpolate(const std::vector<CVC3::Expr>& f, std::vector<CVC3::Expr>& result);
private:

	//!create the file descriptors for communication
	int toFOCI[2],fromFOCI[2];
	
	//!the simplify process id
  	pid_t proc;
  	//!the output stream piped to stdin of the process
  	FILE *out;
  	//!the input stream piped to the stdout of the process
  	FILE *in;

	//!the total size of all the queries made so far
  	size_t totalQuerySize;

	//! remember the variables used in encoding
	map<std::string,CVC3::Expr> symbol_table;

	void Shutdown();
	std::string ReadFromFOCI(int limit=0);
	void WriteToFOCI(const std::string &line);

	std::string toFOCIStringFlatten(const CVC3::Expr& f);

	void getTopLevelConjuncts(const CVC3::Expr& e, std::vector<CVC3::Expr>& result);
};


}

#endif
