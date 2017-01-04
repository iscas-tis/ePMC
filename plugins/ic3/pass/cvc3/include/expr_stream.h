/*****************************************************************************/
/*!
 * \file expr_stream.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Mon Jun 16 10:59:18 2003
 *
 * <hr>
 *
 * License to use, copy, modify, sell and/or distribute this software
 * and its documentation for any purpose is hereby granted without
 * royalty, subject to the terms and conditions defined in the \ref
 * LICENSE file provided with this distribution.
 * 
 * <hr>
 * 
 * Declaration of class ExprStream, an output stream to pretty-print
 * Expr in various nice output formats (presentation/internal/other
 * languages, outo-indentation, bounded depth printing, DAG-ified
 * printing, etc, etc...).
 * 
 * This stream is most useful for the decision procedure designer when
 * writing a pretty-printer (Theory::print() method).  ExprStream
 * carries information about the current output language, and all the
 * indentation and depth pretty-printing is done automagically by the
 * operator<<().
 * 
 */
/*****************************************************************************/

#ifndef _cvc3__expr_stream_h_
#define _cvc3__expr_stream_h_

#include "os.h"
#include "expr.h"

namespace CVC3 {
  class ExprStream;
}

namespace std {
  CVC3::ExprStream& endl(CVC3::ExprStream& os);
}

namespace CVC3 {
  
  /*! \defgroup PrettyPrinting Pretty-printing related classes and methods
   * \ingroup BuildingBlocks
   * If you are writing a theory-specific pretty-printer, please read
   * carefully all the documentation about class ExprStream and its
   * manipulators.
   * 
   * @{
   */

  //! Pretty-printing output stream for Expr.  READ THE DOCS BEFORE USING!
  /*! Use this class as a standard ostream for Expr, string, int,
    Rational, manipulators like endl, and it will do the expected
    thing.  Additionally, it has methods to access the current
    printing flags.
    
    In order for the indentation engine to work correctly, you must
    use the manipulators properly.

    Never use "\\n" in a string; always use endl manipulator, which
    knows about indentation and will do the right thing.

    Always assume that the object you are printing may start printing
    on a new line from the current indentation position.  Therefore,
    no string should start with an empty space (otherwise parts of
    your expression will be mis-indented).  If you need a white space
    separator, or an operator surrounded by spaces, like os << " = ",
    use os << space << "= " instead.  The 'space' manipulator adds one
    white space only if it's not at the beginning of a newly indented
    line.  Think of it as a logical white-space separator with
    intelligent behavior, rather than a stupid hard space like " ".

    Indentation can be set to the current position with os << push,
    and restored to the previous with os << pop.  You do not need to
    restore it before exiting your print function, ExprStream knows
    how to restore it automatically.  For example, you can write:

    os << "(" << push << e[0] << space << "+ " << e[1] << push << ")";

    to print (PLUS a b) as "(a + b)".  Notice the second 'push' before
    the closing paren.  This is intentional (not a typo), and prevents
    the paren ")" from jumping to the next line by itself.  ExprStream
    will not go to the next line if the current position is too close
    to the indentation, since this will not give the expression a
    better look.

    The indentation level is not restored in this example, and that is
    fine, ExprStream will take care of that.

    For complex expressions like IF-THEN-ELSE, you may want to
    remember the indentation to which you want to return later.  You
    can save the current indentation position with os << popSave, and
    restore it later with os << pushRestore.  These manipulators are
    similar to pop and push, but 'pushRestore' will set the new
    indentation level to the one popped by the last popSave instead of
    the current one.  At the moment, there is only one register for
    saving an indentation position, so multiple pushRestore will not
    work as you would expect (maybe this should be fixed?..).

    For concrete examples, see TheoryCore::print() and
    TheoryArith::print().
    
  */
  class CVC_DLL ExprStream {
  private:
    ExprManager* d_em; //!< The ExprManager to use
    std::ostream* d_os; //!< The ostream to print into
    int d_depth; //!< Printing only upto this depth; -1 == unlimited
    int d_currDepth; //!< Current depth of Expr
    InputLanguage d_lang; //!< Output language
    bool d_indent; //!< Whether to print with indentations
    int d_col; //!< Current column in a line
    int d_lineWidth; //!< Try to format/indent for this line width
    //! Indentation stack
    /*! The user code can set the indentation to the current d_col by
      pushing the new value on the stack.  This value is popped
      automatically when returning from the recursive call. */
    std::vector<int> d_indentStack;
    //! The lowest position of the indent stack the user can pop
    size_t d_indentLast;
    //! Indentation register for popSave() and pushRestore()
    int d_indentReg;
    //! Whether it is a beginning of line (for eating up extra spaces)
    bool d_beginningOfLine;
    bool d_dag; //!< Print Expr as a DAG
    //! Mapping subexpressions to names for DAG printing
    ExprMap<std::string> d_dagMap;
    //! New subexpressions not yet printed in a LET header
    ExprMap<std::string> d_newDagMap;
    //! Stack of shared subexpressions (same as in d_dagMap)
    std::vector<Expr> d_dagStack;
    //! Stack of pointers to d_dagStack for pushing/popping shared subexprs
    std::vector<size_t> d_dagPtr;
    //! The smallest size of d_dagPtr the user can `popDag'
    size_t d_lastDagSize;
    //! Flag whether the dagMap is already built
    bool d_dagBuilt;
    //! Counter for generating unique LET var names
    int d_idCounter;
    //! nodag() manipulator has been applied
    bool d_nodag;
    //! Generating unique names in DAG expr
    std::string newName();
    //! Traverse the Expr, collect shared subexpressions in d_dagMap
    void collectShared(const Expr& e, ExprMap<bool>& cache);
    //! Wrap e into the top-level LET ... IN header from the dagMap
    Expr addLetHeader(const Expr& e);
  public:
    //! Default constructor
    ExprStream(ExprManager *em);
    //! Destructor
    ~ExprStream() { }
    //! Set a new output stream
    /*! Note, that there is no method to access the ostream.  This is
      done on purpose, so that DP designers had to use only ExprStream
      to print everything in their versions of Theory::print(). */
    void os(std::ostream& os) { d_os = &os; }
    //! Get the current output language
    InputLanguage lang() const { return d_lang; }
    //! Set the output language
    void lang(InputLanguage l) { d_lang = l; }
    //! Get the printing depth
    int depth() const { return d_depth; }
    //! Set the printing depth
    void depth(int d) { d_depth = d; }
    //! Set the line width
    void lineWidth(int w) { d_lineWidth = w; }
    //! Set the DAG flag (return previous value)
    bool dagFlag(bool flag = true) { bool old = d_dag; d_dag = flag; return old; }
    //! Set the indentation to the current column
    /*! The value will be restorted automatically after the DP print()
      function returns */
    void pushIndent() { d_indentStack.push_back(d_col); }
    //! Set the indentation to the given absolute position
    /*! The value will be restorted automatically after the DP print()
      function returns */
    void pushIndent(int pos) { d_indentStack.push_back(pos); }
    //! Restore the indentation (user cannot pop more than pushed)
    void popIndent();
    //! Reset indentation to what it was at this level
    void resetIndent();
    //! Return the current column position
    int column() const { return d_col; }
    //! Recompute shared subexpression for the next Expr
    void pushDag();
    //! Delete shared subexpressions previously added with pushdag
    void popDag();
    //! Reset the DAG to what it was at this level
    void resetDag();

    // The printing action

    //! Use manipulators which are functions over ExprStream&
    friend ExprStream& operator<<(ExprStream& os,
				  ExprStream& (*manip)(ExprStream&));
    //! Print Expr
    friend ExprStream& operator<<(ExprStream& os, const Expr& e);
    //! Print Type
    friend ExprStream& operator<<(ExprStream& os, const Type& t);
    //! Print string
    friend ExprStream& operator<<(ExprStream& os, const std::string& s);
    //! Print char* string
    friend ExprStream& operator<<(ExprStream& os, const char* s);
    //! Print Rational
    friend ExprStream& operator<<(ExprStream& os, const Rational& r);
    //! Print int
    friend ExprStream& operator<<(ExprStream& os, int i);

    //! Set the indentation to the current position
    friend ExprStream& push(ExprStream& os);
    //! Restore the indentation to the previous position
    friend ExprStream& pop(ExprStream& os);
    //! Remember the current indentation and pop to the previous position
    friend ExprStream& popSave(ExprStream& os);
    //! Set the indentation to the position saved by popSave()
    friend ExprStream& pushRestore(ExprStream& os);
    //! Reset the indentation to the default at this level
    friend ExprStream& reset(ExprStream& os);
    //! Insert a single white space separator
    /*! It is preferred to use 'space' rather than a string of spaces
      (" ") because ExprStream needs to delete extra white space if it
      decides to end the line.  If you use strings for spaces, you'll
      mess up the indentation. */
    friend ExprStream& space(ExprStream& os);
    //! Print the next top-level expression node without DAG-ifying
    /*! 
     * DAG-printing will resume for the children of the node.  This is
     * useful when printing definitions in the header of a DAGified
     * LET expressions: defs have names, but must be printed expanded.
     */
    friend ExprStream& nodag(ExprStream& os);
    //! Recompute shared subexpression for the next Expr
    /*!
     * For some constructs with bound variables (notably,
     * quantifiers), shared subexpressions are not computed, because
     * they cannot be defined outside the scope of bound variables.
     * If this manipulator is applied before an expression within the
     * scope of bound vars, these internal subexpressions will then be
     * computed.
     */
    friend ExprStream& pushdag(ExprStream& os);
    //! Delete shared subexpressions previously added with pushdag
    /*!
     * Similar to push/pop, shared subexpressions are automatically
     * deleted upon a return from a recursive call, so popdag is not
     * necessary after a pushdag in theory-specific print() functions.
     * Also, you cannot pop more than you pushed an the current
     * recursion level.
     */
    friend ExprStream& popdag(ExprStream& os);
    //! Print the end-of-line
    /*! The new line will not necessarily start at column 0 because of
      indentation. 
    
      The name endl will be introduced in namespace std, otherwise
      CVC3::endl would overshadow std::endl, wreaking havoc...
    */
    friend ExprStream& std::endl(ExprStream& os);
  }; // end of class ExprStream

  /*! @} */ // End of group PrettyPrinting

ExprStream& push(ExprStream& os);
ExprStream& pop(ExprStream& os);
ExprStream& popSave(ExprStream& os);
ExprStream& pushRestore(ExprStream& os);
ExprStream& reset(ExprStream& os);
ExprStream& space(ExprStream& os);
ExprStream& nodag(ExprStream& os);
ExprStream& pushdag(ExprStream& os);
ExprStream& popdag(ExprStream& os);


} // End of namespace CVC3

/*
namespace std {
  CVC3::ExprStream& endl(CVC3::ExprStream& os);
}
*/

#endif
