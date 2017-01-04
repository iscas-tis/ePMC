package epmc.ic3.algorithm;

import java.util.PriorityQueue;
import java.util.Queue;

import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.SMTSolver;
import epmc.expression.Expression;
import epmc.model.TSModel;

// PDR implementation of IC3 algorithm 
public class PDR {

	private int k;
	
	private TSModel model;
	
	private SMTSolver lifts = null;
	private SMTSolver init = null;
	
	public PDR(TSModel model) {
		this.model = model;
		this.lifts = model.newSolver();
		this.init = model.newSolver();
		this.k = 1;
	}
	
	
	private int verbose ;
	
	private boolean check() {		
		while(true) {
			if(this.verbose > 1) System.out.println("Level " + k );
			extend();                         // first create F_{k+1}, frontier is F_k
			if(! strengthen()) return false;  // strengthen frontier
			if(propagate()) return true;      // propagate
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
	
	
	private void printStats() {
		// TODO Auto-generated method stub
		
	}


	private boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}

    // remove all states in frontier k that can reach bad states in one-step
	private boolean strengthen() {
		// TODO Auto-generated method stub
		Frame frontier = frames.get(k);
		trivial = true;  // whether any cubes are generated
		earliest = k+1;  // earliest frame with enlarged borderCubes
		while(true) {
			++nQuery;
			// push, Fk /\ T /\ ~P'
			frontier.consecution.push();
			frontier.consecution.addConstraint(model.getPrimed(model
					.getErrorProperty()));
			ConstraintSolverResult rv = frontier.consecution.solve();
			frontier.consecution.pop();
			assert rv != ConstraintSolverResult.UNKNOWN;
			if (rv == ConstraintSolverResult.UNSAT)
				return true; // then P is invariant in Fk
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
	
	private boolean handleObligations(Queue<Obligation> obls) {
		
		while (!obls.isEmpty()) {
			Obligation obl = obls.peek();
			int[] predi = new int[1];
			// whether F(obl.level) /\ ~s /\ T /\ s' is UNSAT
			if (consecution(obl.level, state(obl.state).statesVarAssign,
					obl.state,  predi)) {
				// if UNSAT, then ~s is inductive relative to F(level)
				obls.remove(obl);
				int n = generalize(obl.level, state(obl.state).statesVarAssign);   // not yet
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

	private int generalize(int level, Vec<Expression> cube) {
		// TODO Auto-generated method stub
		do { ++ level;  } while(level <= k && consecution(level, cube)) ;
		addCube(level, cube);
		return level;
	}
	
	private void addCube(int level, Vec<Expression> cube) {
		addCube(level, cube, true);
	}

	// be careful to implement addCube, since we should consider that how to 
	// implement CubeSet, whether F(k) is a subset of F(k+1) or just let 
	// F(k+1) = F(k) union F(k+1), also note that how to maintain informations
	// in consecution solver
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
        }
	}
	
	private boolean consecution(int level, Vec<Expression> states) {
		return consecution(level, states, 0, null);
	}

	private boolean consecution(int fi, Vec<Expression> states,
			int succ, int[] predi) {
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
	      ++nQuery; 
	      // push
	      fr.consecution.push();
	      fr.consecution.addConstraint(assumps);
	      ConstraintSolverResult rv = fr.consecution.solve();
	      fr.consecution.pop();
	      // pop
	      if (rv == ConstraintSolverResult.SAT) {
	        // fails: extract predecessor(s)
	        if (predi != null) predi[0] = stateOf(fr, succ);
	        return false;
	      }

	      return true;
	}

	private int stateOf(Frame fr) {
		return stateOf(fr, 0);
	}
	private int stateOf(Frame fr, int succ) {
		return succ;
	}


	private int nQuery;
	private boolean trivial ;
    private int earliest ;
    private int nCTI;
    private int cexState;
    
	private void extend() {
		// TODO Auto-generated method stub
		while(frames.size() < k + 2) {
			frames.push(new Frame());
			Frame fr = frames.back();
			fr.k = frames.size() - 1;
			fr.consecution = model.newSolver();
			if(fr.k == 0) fr.consecution.addConstraint(model.getInitialStates());
			assertTransitions(fr.consecution);
		}
	}


	private boolean check(int verbose) {
		return true;
	}
	
	private void assertTransitions(SMTSolver solver) {
		solver.addConstraint(model.getInvariants());
		solver.addConstraint(model.getPrimed(model.getInvariants()));
		solver.addConstraint(model.getTransitions());
	}

} 
