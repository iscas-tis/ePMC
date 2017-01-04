#ifndef GAME_SPARSE_H
#define GAME_SPARSE_H

#define METHOD_RELATIVE 0
#define METHOD_ABSOLUTE 1
#define METHOD_EXPONENT 2

namespace model_checker {
  class GameSparse {
    public:

    /* nr of states */
    inline unsigned getNrOfStates() const { return stateStart.size(); } 
    /* nr of choice sets (level player 1) */
    inline unsigned getNrOfChoiceSets() const { return choiceSetStart.size(); }
    /* nr of choices (level player 2) */
    inline unsigned getNrOfChoices() const { return choiceSetStart.size(); }
    /* nr of distributions (level player 2 1/2*/
    inline unsigned getNrOfDistributions() const { return distributionStart.size(); }
    /* nr of matrix entries */
    inline unsigned getNrOfNonZeros() const { return distributionProb.size(); }

    static bool approxEquality(double a, double b, int precision);
  private:
    friend class GameGraph;
    bool checkConvergence(std::vector<double> &, std::vector<double> &);
    GameSparse(unsigned term_crit = METHOD_EXPONENT,
    	       double term_crit_param = 1E-06,
    	       unsigned max_iters = 10000);
    void clear();
    void print();
    void setTermCritMethod(unsigned __term_crit);
    void setTermCritParam(double __term_crit_param);
    void setMaxIters(unsigned __max_iters);

    void until(bool P1min, bool P2min, std::vector<double>& soln,
	       std::vector<int> &chosenP1, std::vector<int> &chosenP2);

    void CTUntil(bool P2min, std::vector<double>& lower,
		 std::vector<double>& upper);

    /* stateStart tells at which index outgoint transitions of a state
     * start. End point is starting point of next state. Because of this,
     * there is one more index than states exist
     */
    std::vector<int> stateStart;
    /* stateChoiceSet gives the sets of choices for each state. Start and
     * end index is given by stateStarts.
     */
    std::vector<int> stateChoiceSet;

    /* choiceSetStart gives for each choice set where the distributions of it
     * start end end.
     */
    std::vector<int> choiceSetStart;
    /* distributions of each choice set*/
    std::vector<int> choiceSetDistribution;

    /* start and end of distributions */
    std::vector<int> distributionStart;
    /* target state of probabilistic choice */
    std::vector<int> distributionTarget;
    /* probability of probabilistic choice */
    std::vector<double> distributionProb;

    /* bad states */
    std::vector<bool> badStates;

    /* Notice that i had to change the structure quite much to allow sharing.
     * In the original structure this would have been impossible. The
     * disadvantage here is that more arrays are needed, leading to waste of
     * space and probably worse runtime if no sharing is used. However, if
     * sharing is used, this should be better.
     */

    unsigned term_crit;
    double term_crit_param;
    unsigned max_iters;
    int precision;

    /* for time-bounded reachability for CTMDPs */
    std::vector<double> P_s_alpha_B;
    double rate;
  };
}

#endif
