package epmc.sat;

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sun.jna.Pointer;

import epmc.error.EPMCException;
import epmc.error.ProblemsEPMC;
import epmc.util.JNATools;

public class MiniSAT implements SatSolver {
    
    private Pointer satSolver;
    private final static int MINISAT_L_TRUE = 1;
    private final static int MINISAT_L_FALSE = 0;
    private final static int MINISAT_L_UNDEF = -1;
    private boolean pushin = false;
    private Lit pushLit = null;
    private Lit minisat_lit = new MiniSat_Lit();
    MiniSAT() throws EPMCException {
        /* load fail issure*/
        ensure(MiniSat.loaded, ProblemsEPMC.MINISAT_NATIVE_LOAD_FAILED);
        satSolver = MiniSat.minisat_new();
        assert satSolver != null;
    }

    @Override
    public int new_var() {
        // TODO Auto-generated method stub
        return MiniSat.minisat_newVar(satSolver);
    }

    @Override
    public void addClause(List cls) {
        // TODO Auto-generated method stub
       MiniSat.minisat_addClause_begin(satSolver);
       for(Object p : cls) {
           Lit lit = (Lit) p;
           MiniSat.minisat_addClause_addLit(satSolver, lit.toInt());
       }
       MiniSat.minisat_addClause_commit(satSolver);
    }

    @Override
    public void addClause(Lit p) {
        // TODO Auto-generated method stub
        MiniSat.minisat_addClause_begin(satSolver);
        MiniSat.minisat_addClause_addLit(satSolver, p.toInt());
        MiniSat.minisat_addClause_commit(satSolver);
    }

    @Override
    public void addClause(Lit p1, Lit p2) {
        // TODO Auto-generated method stub
        MiniSat.minisat_addClause_begin(satSolver);
        MiniSat.minisat_addClause_addLit(satSolver, p1.toInt());
        MiniSat.minisat_addClause_addLit(satSolver, p2.toInt());
        MiniSat.minisat_addClause_commit(satSolver);
    }

    @Override
    public void addClause(Lit p1, Lit p2, Lit p3) {
        // TODO Auto-generated method stub
        MiniSat.minisat_addClause_begin(satSolver);
        MiniSat.minisat_addClause_addLit(satSolver, p1.toInt());
        MiniSat.minisat_addClause_addLit(satSolver, p2.toInt());
        MiniSat.minisat_addClause_addLit(satSolver, p3.toInt());
        MiniSat.minisat_addClause_commit(satSolver); 
    }

    @Override
    public boolean solve(List assums) {
        // TODO Auto-generated method stub
        MiniSat.minisat_solve_begin(satSolver);
        for(Object p : assums) {
            Lit lit = (Lit)p;
            MiniSat.minisat_solve_addLit(satSolver, lit.toInt());
        }
        
        int rv = MiniSat.minisat_solve_commit(satSolver);
        
        return rv == 1 ? true : false;
    }

    @Override
    public Lbool getModelValue(Lit p) {
        // TODO Auto-generated method stub
        int rv = MiniSat.minisat_modelValue_Lit(satSolver, p.toInt());
        switch(rv) 
        {
        case MINISAT_L_TRUE:
            return Lbool.l_True;
        case MINISAT_L_FALSE:
            return  Lbool.l_False;
        default:
            break;
        }
        return Lbool.l_Undef;
    }

    @Override
    public Set getUnsatCore() {
        // TODO Auto-generated method stub
        int num = MiniSat.minisat_conflict_len(satSolver);
        Set<Lit> unsatCore = new HashSet<Lit>();
        for(int i = 0; i < num ; i ++) {
            int lit = MiniSat.minisat_conflict_nthLit(satSolver, i);
            Lit clf = minisat_lit.fromInt(lit);
            if(pushin && pushLit != null &&
                    pushLit.var() == clf.var()) {
                continue;
            }
            unsatCore.add(clf);
        }
        return unsatCore;
    }

    @Override
    public void pushvar() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void popvar() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFrozen(int v) {
        // TODO Auto-generated method stub
        int num = MiniSat.minisat_num_vars(satSolver);
        assert v < num;
        MiniSat.minisat_setFrozen(satSolver, v, 1);
    }

    @Override
    public void unfreezeAll() {
        // TODO Auto-generated method stub
        int num = MiniSat.minisat_num_vars(satSolver);
        for(int v = 0 ; v < num ; v ++) {
            MiniSat.minisat_setFrozen(satSolver, v, 0);
        }
    }

    @Override
    public void preprocess() {
        // TODO Auto-generated method stub
        MiniSat.minisat_eliminate(satSolver, 1);
    }

    @Override
    public int resetFreq() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    private static final class MiniSat_Lit implements Lit {

        int x ; /* number */
        
        MiniSat_Lit(int var, boolean neg) {
            x = var << 1;
            x = neg ? (x | 1) : x; 
        }
        
        MiniSat_Lit() {
            x = -1;
        }
        
        @Override
        public Lit mkLit(int var, boolean neg) {
            // TODO Auto-generated method stub
            Lit p = new MiniSat_Lit(var, neg);
            return p;
        }

        @Override
        public int var() {
            // TODO Auto-generated method stub
            return x >> 1;
        }

        @Override
        public Lit clone() {
            // TODO Auto-generated method stub
            return fromInt(this.toInt());
        }

        @Override
        public Lit not() {
            // TODO Auto-generated method stub
            Lit p = fromInt(this.toInt() ^ 1);
            return p;
        }

        @Override
        public Lit fromInt(int p) {
            // TODO Auto-generated method stub
            boolean neg = (p & 1) == 0 ? false : true;
            int var = p >> 1;
            Lit lit = new MiniSat_Lit(var, neg);
            return lit;
        }
        
        public int hashCode() {
            return toInt();
        }

        @Override
        public int toInt() {
            // TODO Auto-generated method stub
            return x;
        }

        @Override
        public boolean undef() {
            // TODO Auto-generated method stub
            return x == -1;
        }

        @Override
        public int compareTo(Object o) {
            // TODO Auto-generated method stub
            Lit p = (Lit)o;
            if(p.toInt() == this.toInt())
                return 0;
            else {
                if(this.toInt() < p.toInt()) return -1;
            }
            
            return 1;
        }
        
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if(sign()) builder.append("~");
            builder.append("v" + var());
            return builder.toString();
        }
        
    }
    
    private static final class MiniSat {
        
        static native Pointer minisat_new();
        static native void  minisat_delete(Pointer solver);
        static native int   minisat_newVar(Pointer solver);
        static native int   minisat_newLit(Pointer solver);
        static native int   minisat_mkLit(int x);
        static native int   minisat_mkLit_args(int x, int sign);
        static native int   minisat_negate(int p);
        static native int   minisat_var(int p);
        static native int   minisat_sign(int p);
        static native void  minisat_addClause_begin (Pointer solver);
        static native void  minisat_addClause_addLit(Pointer solver, int p);
        static native int   minisat_addClause_commit(Pointer solver);
        static native int   minisat_simplify (Pointer solver);

        // NOTE: Currently these run with default settings for implicitly calling preprocessing. Turn off
        // before if you don't need it. This may change in the future.
        static native void  minisat_solve_begin     (Pointer solver);
        static native void  minisat_solve_addLit    (Pointer solver, int p);
        static native int   minisat_solve_commit    (Pointer solver);
        static native int   minisat_limited_solve_commit (Pointer solver);

        static native int   minisat_okay     (Pointer solver);
        static native void  minisat_setPolarity     (Pointer solver, int v, int lb);
        static native void  minisat_setDecisionVar  (Pointer solver, int v, int b);
        static native  int  minisat_value_Var      (Pointer solver, int x);
        static native  int  minisat_value_Lit      (Pointer solver, int p);
        static native  int  minisat_modelValue_Var (Pointer solver, int x);
        static native  int  minisat_modelValue_Lit (Pointer solver, int p);
        static native  int  minisat_num_assigns     (Pointer solver);
        static native  int  minisat_num_clauses     (Pointer solver);
        static native  int  minisat_num_learnts     (Pointer solver);
        static native  int  minisat_num_vars        (Pointer solver);
        static native  int  minisat_num_freeVars    (Pointer solver);
        static native  int  minisat_conflict_len    (Pointer solver);
        static native  int  minisat_conflict_nthLit (Pointer solver, int i);
        static native  void minisat_set_verbosity   (Pointer solver, int v);
        static native  int  minisat_get_verbosity   (Pointer solver);
        static native  int  minisat_num_conflicts   (Pointer solver);
        static native  int  minisat_num_decisions   (Pointer solver);
        static native  int  minisat_num_restarts    (Pointer solver);
        static native  int  minisat_num_propagations(Pointer solver);
        static native  void minisat_set_conf_budget (Pointer solver, int x);
        static native  void minisat_set_prop_budget (Pointer solver, int x);
        static native  void minisat_no_budget       (Pointer solver);

        // Resource constraints:
        static native  void minisat_interrupt(Pointer solver) ;
        static native  void minisat_clearInterrupt(Pointer solver) ;

        // SimpSolver methods:
        static native  void minisat_setFrozen       (Pointer solver, int v, int b) ;
        static native  int  minisat_isEliminated    (Pointer solver, int v) ;
        static native  int  minisat_eliminate       (Pointer solver, int turn_off_elim);
        private final static boolean loaded =
                JNATools.registerLibrary(MiniSat.class, "minisat");
    }
    
    public static void test() throws EPMCException {
        SatSolver solver = new MiniSAT();
        
        int x1 = solver.new_var();
        int x2 = solver.new_var();
        
        solver.addClause(new MiniSat_Lit(x1, false));
        solver.addClause(new MiniSat_Lit(x2, false));
        
        solver.addClause(new MiniSat_Lit(x1, true), new MiniSat_Lit(x2, true));
        
        boolean rv = solver.solve(new ArrayList());
        
        System.out.println("SAT? = " + rv);
    }

}
