#ifndef FOCIPARSER_H_
#define FOCIPARSER_H_

namespace dp {

class FOCIParser {
	public:
	FOCIParser(const map<std::string,CVC3::Expr>& __symbol_table);
	void parseSequence(std::string, std::vector<CVC3::Expr>& result);
	private:
	map<std::string,CVC3::Expr> symbol_table;
	CVC3::Expr parseTerm();
	void parseTermList(vector<CVC3::Expr>& term_lst);
	CVC3::Expr parseFormula();
	void parseFormulaList(vector<CVC3::Expr>& term_lst);
	std::string in_stream;
	std::string seen;
	std::string::iterator current;
	std::map<int,CVC3::Expr> expr_table;
	std::map<int,CVC3::Expr> pred_table;
	bool is_whitespace(char token);
	bool is_ctrlChar(char token);
	void next_char();
	void next_token();
	// to use after buffer_to_next_ws
	void next_token2();
	// returns the chars until the next whitespace or the next control char
	std::string buffer_to_next_ws();
};

}

#endif
