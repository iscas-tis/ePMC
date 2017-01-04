#ifndef CSIsat_H_
#define CSIsat_H_

namespace dp {

class CSIsat : public SMT {
	public:
	//! invoke the CSIsat-Prover process
	CSIsat();
	//! kill the CSIsat-Prover process
	virtual ~CSIsat();

	/*! get the expression vector (conjunction) as a string that CSIsat understands */
	std::string toCSIsatString(const vector<CVC3::Expr>& v);

	/*! get the expression as a string that CSIsat understands */
	std::string toCSIsatString(const CVC3::Expr& e);

	/*! \brief compute interpolant
	*/
  	virtual lbool Interpolate(const std::vector<CVC3::Expr>& f, std::vector<CVC3::Expr>& result);
private:

	//!create the file descriptors for communication
	int toCSIsat[2],fromCSIsat[2];

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
	std::string ReadFromCSIsat();
	
	void invokeCSIsat();
	void terminateCSIsat();

	void WriteToCSIsat(const std::string &line);

	std::string toCSIsatStringFlatten(const CVC3::Expr& f);
	int in_read ;
	int in_write ;
	int out_read ;
	int out_write ;
};


}

#endif
