package epmc.ic3.algorithm;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.SMTSolver;
import epmc.expression.Expression;
import epmc.model.TSModel;
import epmc.value.OperatorNames;
import epmc.value.Value;


/**
 * This is an very simple implementation of IC3 algorithm 
 * adapted form the version given by Aaron R. Bradley
 * TODO: 
 * 1 To achieve better performance, pre-image of CTIs should be computed efficiently and
 * include as many states as possible, right now the under-approximate do not work well
 * 2 To extract UNSAT CORE from CTIs, to include as many states as possible
 * 3 Z3 does not support general expressions as assumptions, so I use push and pop to 
 * mimic this function, it should be changed once we have a better alternative
 * */ 
public class IC3 {
	private int k;
	
	private TSModel model;
	
	public IC3(TSModel model) {
		this.model = model;
		this.lifts = model.newSolver();
		this.init = model.newSolver();
		this.k = 1;
		this.maxDepth = 1;
	}
	// main check function
	private boolean check() {		
		while(true) {
			if(this.verbose > 1) System.out.println("Level " + k );
			extend();                         // extend the border, get another frontier Frame
			if(! strengthen()) return false;  // strengthen the frontier by one-step CTIs
			if(propagate()) return true;      // propagate clauses in frontier
			printStats();
			++ k;
		}
	}
	
	public void close() {
		for(int i = 0; i < frames.size() ; i ++) {
			frames.get(i).consecution.close();
		}
		lifts.close();
		init.close();
	}
	
	private static class Frame {
		int k;                       // distance from initial states
	    CubeSet<Vec<Expression>> borderCubes; // additional cubes in this and previous frames
		SMTSolver consecution;
		
		public Frame() {
			borderCubes = new TreeCubeSet<Vec<Expression>>();
		}
	}
	
	Vec<Frame> frames = new ArrayVec<>();
	SMTSolver lifts;
	
	private class State {
		int succ;
		Vec<Expression> statesVarAssign;
		Expression actionVarAssign;
		int index;
		boolean used;
		public State() {
			statesVarAssign = new ArrayVec<Expression>();
		}
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("action=" + actionVarAssign);
			for(int i = 0; i < statesVarAssign.size() ; i ++) {
				builder.append(", " + statesVarAssign.get(i));
			}
			builder.append("\n");
			return builder.toString();
		}
	}
	
	Vec<State> states = new ArrayVec<>();
	int nextState = 0;
	State state(int index) { return states.get(index - 1); } 
	int newState() {
	      if (nextState >= states.size()) {
	          states.push(new State());
	          states.back().index = states.size();
	          states.back().used = false;
	        }
	        int ns = nextState;
	        assert (!states.get(ns).used);
	        states.get(ns).used = true;
	        while (nextState < states.size() && states.get(ns).used)
	          nextState++;
	        return ns+1;
	}
    void delState(int sti) {
        State st = state(sti);
        st.used = false;
        st.statesVarAssign.clear();
        st.actionVarAssign = null;
        if (nextState > st.index-1) nextState = st.index-1;
    }
    void resetStates() {
        for (int i = 0; i < states.size() ; i ++) {
        	State st = states.get(i);
        	st.used = false;
            st.actionVarAssign = null;
            st.statesVarAssign.clear();
        }
        nextState = 0;
    }
    
    // Propagates clauses forward using induction.  If any frame has
    // all of its clauses propagated forward, then two frames' clause
    // sets agree; hence those clause sets are inductive
    // strengthenings of the property.  See the four invariants of IC3
    // in the original paper.
	private boolean propagate() {
	      if (verbose > 1) System.out.println("propagate ...");
	      // 1. clean up: remove c in frame i if c appears in frame j when i < j
	      CubeSet<Vec<Expression>> all = new TreeCubeSet<>();
	      // all set includes all cubes in i+1 , i + 2 , ..., j (max)
	      // then f(i).borderCubes - all will be the cubes in f(i)
	      for (int i = k+1; i >= earliest; --i) {
	          Frame  fr = frames.get(i);
	          CubeSet<Vec<Expression>> nall, rem = fr.borderCubes.diff(fr.borderCubes, all);
	          if (verbose > 1)
	             System.out.println("diff->: " + i + " " + fr.borderCubes.size() + " " + rem.size() + " ");
	          fr.borderCubes.swap(rem);   // get remainder
	          nall = rem.union(rem, all); 
	          all.swap(nall);             // union f(i)
	          Iterator<Vec<Expression>> iter = fr.borderCubes.iterator();
	          while(iter.hasNext()) {
	        	  Vec<Expression> cube = iter.next();
	        	  assert all.contains(cube) == true;
	          }
	            
	          if (verbose > 1)
	            System.out.println("size of all: " + all.size());
	      }
	      // 2. check if each c in frame i can be pushed to frame j
	      // if trivial that means, query F(k) /\ T /\ ~P' is UNSAT, 
	      // all clauses in level k should be directly tried to push forward
	      // ckeep: number of clauses kept in original level,
	      // cprop: number of clauses pushed forward
	      for (int i = trivial ? k : 1; i <= k; ++i) {
	          int ckeep = 0, cprop = 0;
	          Frame fr = frames.get(i);
	          Iterator<Vec<Expression>> iter = fr.borderCubes.iterator();
	          CubeSet<Vec<Expression>> dups = new TreeCubeSet<>();
	          while(iter.hasNext()) {
		            Vec<Expression> core = iter.next();
		            if (consecution(i, core, 0, null, null)) {
		              ++cprop;
		              // only add to frame i+1 unless the core is reduced
		              addCube(i+1, core, false);
		              dups.insert(core);
		            }
		            else {
		              ++ckeep;
		            }
	          }
              fr.borderCubes.removeAll(dups);
	          if (verbose > 1)
	            System.out.println( "level=" + i + " keep_c=" + ckeep + " prop_c=" + cprop );
	       // all has been pushing forward, we have found fixed point, keep_c = 0
	          if (fr.borderCubes.isEmpty()) {
	        	  if (verbose > 1) 
	        		  System.out.println("all clauses at level " + fr.k 
	        				  + " have been pushed forward...");
	              return true;
	          }
	        }
	      // 3. simplify frames, no implementation yet, 
	      // I do not know such functionality in Z3
	      return false;
	}
	
	boolean trivial ;  // indicate whether strenthening is required
	int earliest;      // track earliest modified level in a major iteration
	
	private class Obligation implements Comparable<Object> {
		int state, level, depth;
		Obligation(int state, int level, int depth) {
			this.state = state;
			this.level = level;
			this.depth = depth;
		}
		@Override
		public int compareTo(Object o) {
			Obligation o2 = (Obligation)o;
	        if (this.level < o2.level) return -1;  // prefer lower levels (required)
	        if (this.level > o2.level) return 1;
	        if (this.depth < o2.depth) return -1;  // prefer shallower (heuristic)
	        if (this.depth > o2.depth) return 1;
	        if (this.state < o2.state) return -1;  // canonical final decider
	        return 0;
		}// we can change this part
	}
	// Strengthens frontier to remove error successors
    private boolean strengthen() {
      Frame frontier = frames.get(k);
      trivial = true;  // whether any cubes are generated
      earliest = k+1;  // earliest frame with enlarged borderCubes
      while (true) {
        ++nQuery; startTimer();  // stats
        // push 
        
        frontier.consecution.push();
        frontier.consecution.addConstraint(model.getPrimed(model.getErrorProperty()));
        ConstraintSolverResult rv = frontier.consecution.solve();
//        System.out.println("solver1: " + frontier.consecution.toString());
        frontier.consecution.pop();
//        System.out.println("solver1: " + frontier.consecution.toString());
//        ConstraintSolverResult rv = frontier.consecution
//        		.solve(model.getPrimed(model.getErrorProperty())); // Fk /\ T /\ ~P'
        assert rv != ConstraintSolverResult.UNKNOWN;
        endTimer(satTime);
        if (rv == ConstraintSolverResult.UNSAT) return true;       // then P is invariant
        // handle CTI with error successor
        ++nCTI;  // stats
        trivial = false;
        Queue<Obligation> pq = new PriorityQueue<>();
        // enqueue main obligation and handle
        pq.add(new Obligation(stateOf(frontier), k-1, 1));
        if (!handleObligations(pq))
          return false;
        // finished with States for this iteration, so clean up
        resetStates();
      }
    }
    
    // Process obligations according to priority.
    boolean handleObligations(Queue<Obligation> obls) {
    	
		while (!obls.isEmpty()) {
			Obligation obl = obls.peek();
			Vec<Expression> core = new ArrayVec<>();
			int[] predi = new int[1];
			// Is the obligation fulfilled?
			if (consecution(obl.level, state(obl.state).statesVarAssign,
					obl.state, core, predi)) {
				// Yes, so generalize and possibly produce a new obligation
				// at a higher level.
				obls.remove(obl);
				core = core.size() == 0 ? state(obl.state).statesVarAssign : core;
				int n = generalize(obl.level, core);   // not yet
				if (n <= k)
					obls.add(new Obligation(obl.state, n, obl.depth));
			} else if (obl.level == 0) {
				// No, in fact an initial state is a predecessor.
				cexState = predi[0];
				return false;
			} else {
				++nCTI; // stats
				// No, so focus on predecessor.
				obls.add(new Obligation(predi[0], obl.level - 1, obl.depth + 1));
			}
		}
		return true;
    }
    // we can do better by pushing cube to the highest level
    private int generalize(int level, Vec<Expression> cube) {
		// TODO Auto-generated method stub
        // push
        do { ++level; } while (level <= k && consecution(level, cube));
        // level may not hold when level = k + 1, but since F(level-1) /\ ~s /\ T /\ s' is UNSAT
        // this means that ~s will be added to F(level) to further strengthen the state space 
        // so we only add cube to F(level).cubes and do not add them to lower level.
        addCube(level, cube);
        return level;
	}
    // add to frame at level and below if toAll is true, else we only 
    // add to frame at level 
    private void addCube(int level, Vec<Expression> cube, boolean toAll) {
    	CubeSet< Vec<Expression> > cubeSet = frames.get(level).borderCubes;
        boolean rv = cubeSet.insert(cube);
        if (!rv) return;          // already exists
        if (verbose > 1) 
           System.out.println("level=" + level + " added cube: " + cube );
        earliest = earliest < level ? earliest : level;
        Expression cls = model.getContext().getFalse();
        for(int i = 0 ; i < cube.size() ; i ++) {
        	cls = cls.or(cube.get(i).not());
        }

        for (int i = toAll ? 1 : level; i <= level; ++i) {
           frames.get(i).consecution.addConstraint(cls);    
           // since cls will only pushed to highest level which may not 
           // have F(level) /\ ~ s /\ T /\ s' UNSAT, so we do not add cube to borderCubes
           // at lower level 
//           frames.get(i).borderCubes.insert(cube);        // 0 < j < level 
        }
    }
    // Adds cube to frames at and below level, unless !toAll, in which
    // case only to level.
    private void addCube(int level, Vec<Expression> cube) {
		// TODO Auto-generated method stub
        addCube(level, cube, true);
	}

	boolean consecution(int fi, Vec<Expression> states) {
    	return consecution(fi, states, 0, null, null);
    }

    // Check if ~latches is inductive relative to frame fi.  If it's
    // inductive and core is provided, extracts the unsat core.  If
    // it's not inductive and pred is provided, extracts
    // predecessor(s). TODO: extract unsat core?
	private boolean consecution(int fi, Vec<Expression> states,
			int succ, Vec<Expression> core, int[] predi) {
	      Frame  fr = frames.get(fi);
          Expression cls = model.getContext().getFalse();
          Expression assumps = model.getContext().getTrue();
	      //
	      for(int i = 0; i < states.size() ; i ++) {
	    	  cls = cls.or(states.get(i).not());
	    	  assumps = assumps.and(model.getPrimed(states.get(i)));
	      }
	      // F_fi & ~latches & T & latches'
	      fr.consecution.addConstraint(cls);
	      ++nQuery; startTimer();  // stats
//	      ConstraintSolverResult rv = fr.consecution.solve(assumps);
	      // push
	      fr.consecution.push();
	      fr.consecution.addConstraint(assumps);
	      ConstraintSolverResult rv = fr.consecution.solve();
	      fr.consecution.pop();
	      // pop
	      endTimer(satTime);
	      if (rv == ConstraintSolverResult.SAT) {
	        // fails: extract predecessor(s)
	        if (predi != null) predi[0] = stateOf(fr, succ);
	        return false;
	      }
	      // succeeds
	      if (core != null) {
	    	  
	      }
	      return true;
	}

	// Assumes that last call to fr.consecution->solve() was
    // satisfiable.  Extracts under-approximation of the exact
    // pre-image of state(s) cube from satisfying
    // assignment."
    int stateOf(Frame fr, int succ) {
        // create state
        int st = newState();
        State curState = state(st);
        curState.succ = succ;
        // if succ == 0, then error states are direct successors 
        // else we use state(succ) as successors 
        Expression primedState ;
        if(succ == 0) {
        	primedState = model.getPrimed(model.getErrorProperty());
        }else { // get its state(succ)
        	Vec<Expression> assigns = state(succ).statesVarAssign;
        	primedState = model.getPrimed(assigns.get(0));
        	for(int idx = 1; idx < assigns.size() ; idx ++) {
        		primedState = primedState.and(model.getPrimed(assigns.get(idx)));
        	}
        }
        // get pre-image of bad states
        Expression[] primdVars = model.getVariables(true);
        Expression[] curVars = model.getVariables(false);
        boolean hasState = false;
        for(Expression tr : model.getTransitionsDNF()) {
        	lifts.push();
        	lifts.addExists(primdVars, primedState.and(tr));
        	++nQuery;
        	startTimer();  // stats
        	ConstraintSolverResult rv = lifts.solve();
        	
        	endTimer(satTime);
        	Expression assign = model.getContext().getTrue();
        	Vec<Expression> stateLits = new ArrayVec<>();
        	Expression actionLit = null;
        	Expression notStates = model.getContext().getFalse();
        	if(rv == ConstraintSolverResult.SAT) {
        		
        		// check whether assignment can be add to F
        		for(int i = 0; i < curVars.length ; i ++) {
        			Value val = lifts.getModelValue(curVars[i]);
        			if(val != null){
        				Expression lit = model.getContext()
        						.newOperator(OperatorNames.EQ, curVars[i]
        						, model.getContext().newLiteral(val));
        				assign = assign.and(lit);
        				stateLits.push(lit);
        				notStates = notStates.or(lit.not());
        			}
        		}
        		Value actVal = lifts.getModelValue(model.getActionVariables());
        		assert actVal != null;
        		actionLit = model.getContext()
						.newOperator(OperatorNames.EQ, model.getActionVariables()
						, model.getContext().newLiteral(actVal));
        		assign = assign.and(actionLit);
        		// push 
        		fr.consecution.push();
        		fr.consecution.addConstraint(assign);
        		if(fr.consecution.solve() == ConstraintSolverResult.SAT)
        			hasState = true;
        		fr.consecution.pop();
        	}
        	lifts.pop();
        	if(hasState) {
        		curState.actionVarAssign = actionLit;
        		curState.statesVarAssign = stateLits;
        		lifts.addConstraint(notStates); // need to remove bad states
        		break;
        	}
        }
        return st;
    }
    // OK, compute pre-image of s in Frame fr by abstracting out all 
    // primed variables
    int stateOf(Frame fr) {
    	return stateOf(fr, 0);
    }
    // TODO: still do not know how to make a better pre-image
    void underPreImage(Frame fr, int succ) { }
    
    /* push forward */
	private void extend() {
		while(frames.size() < k + 2) {
			frames.push(new Frame());
			Frame fr = frames.back();
			fr.k = frames.size() - 1;
			fr.consecution = model.newSolver();
			if(fr.k == 0) fr.consecution.addConstraint(model.getInitialStates());
			assertTransitions(fr.consecution);
		}
	}
	SMTSolver init ;
	// check whether it is in initial states
	private boolean isInitiation(Expression... expressions) {
		if(init == null)  {
			init = model.newSolver();
		}
		init.addConstraint(model.getInitialStates());
		
		Expression assumps = null;
		for(Expression expr : expressions) {
			assumps = assumps == null ? expr : assumps.and(expr);
		}
		init.push();
		init.addConstraint(assumps);
		ConstraintSolverResult rv = init.solve();
		init.pop();
		return rv == ConstraintSolverResult.SAT;
	}
	long startTime, satTime;
	/* some statistical data */
	int nQuery, nCTI, nmic;
	int nCoreReduced, nAbortJoin, nAbortMic;
	int maxDepth;
	boolean random;
	int verbose;
	int cexState;
	
	// IC3 does not check for 0-step and 1-step reachability analysis
	boolean baseCases() {
		// I /\ ~P
		SMTSolver base0 = this.model.newSolver();
		base0.addConstraint(this.model.getInitialStates());
		base0.addConstraint(this.model.getInvariants());
		base0.addConstraint(this.model.getErrorProperty());
		ConstraintSolverResult rv = base0.solve();
		assert rv != ConstraintSolverResult.UNKNOWN;
		base0.close();
		if(rv == ConstraintSolverResult.SAT) return false;
		
		// I(x) /\ T(x, x') /\ ~P'
		SMTSolver base1 = this.model.newSolver();
		base1.addConstraint(this.model.getInitialStates());
		assertTransitions(base1);
		base1.addConstraint(this.model.getPrimed(this.model.getErrorProperty()));
		rv = base1.solve();
		assert rv != ConstraintSolverResult.UNKNOWN;
		base1.close();
		if(rv == ConstraintSolverResult.SAT) return false;
		return true;
	}
	// can be accessed by other class
	public boolean check(int verbose, boolean basic, boolean random) {
		if(! baseCases()) 
			return false;
		if(basic) {
			this.maxDepth = 0;
		}
		this.verbose = verbose;
		if(random) this.random = true;
		boolean rv = check();
		if(! rv && verbose > 1) printWitness();
		printStats();
		return rv;
	}

	
	//------------------ Until function ----------------
	private void printWitness() {
		System.out
				.println("--------------- Counterexample path --------------");
		int stateIdx = 1;
		if (cexState != 0) {
			int curr = cexState;
			while (curr != 0) {
				System.out.println("state idx=" + stateIdx + ": "
						+ state(curr).actionVarAssign.toString() + " "
						+ state(curr).statesVarAssign.toString());
				curr = state(curr).succ;
				++ stateIdx;
			}
		}
		System.out
				.println("--------------- Counterexample path --------------");
	}
	// help for time function
	private long timer;
	public void startTimer() {
		this.timer = time(); 
	}
	
	public long endTimer() {
		return (time() - this.timer);
	}
	
	public long endTimer(long time) {
		return time + (time() - this.timer);
	}
	
	private long time() {
		return System.currentTimeMillis();
	}
	
	private long toSeconds(long timeUnit) {
		return TimeUnit.MILLISECONDS.toSeconds(timeUnit);
	}
	
	private void printStats() {
		if(this.verbose == 0) return ;
		long time = time() - startTime;
		System.out.println(". Elapsed time:    " + toSeconds(time - startTime));
		if(time == 0) time = 1;
		System.out.println(". % SAT time:      " + 100 * (satTime * 1.0 / time));
		System.out.println(". K :              " + k);
		System.out.println(". # Queries :      " + nQuery);
		System.out.println(". # CTIs :         " + nCTI);
		System.out.println(". # Red. cores :   " + nCoreReduced);
		System.out.println(". # Int. joins :   " + nAbortJoin);
		System.out.println(". # Int. mics :    " + nAbortMic);
	}
	
	private void assertTransitions(SMTSolver solver) {
		solver.addConstraint(model.getInvariants());
		solver.addConstraint(model.getPrimed(model.getInvariants()));
		solver.addConstraint(model.getTransitions());
	}
	
	
	
	
	

}
