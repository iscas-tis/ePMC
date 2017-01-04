package epmc.ic3.command;

import java.util.Locale;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import epmc.constraintsolver.ConstraintSolverSMT;
import epmc.constraintsolver.Z3SMT;
import epmc.error.EPMCException;
import epmc.expression.CmpType;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.graph.Semantics;
import epmc.ic3.algorithm.IC3;
import epmc.model.PrismModel;
import epmc.model.TSModel;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.ModelCheckerResult;
import epmc.modelchecker.Properties;
import epmc.modelchecker.RawProperty;
import epmc.options.Options;
import epmc.prism.model.ModelPRISM;
import epmc.prism.model.PredModel;

public class CommandTaskCheckIC3 implements CommandTask {
    public final static String IDENTIFIER = "checkic3";
    private ModelChecker modelChecker;
    private ContextExpression context ;
    private Options options;
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    private void setupEnv() {
        this.context = modelChecker.getContextExpression();
        this.options = context.getOptions();
    }
    
    @Override
    public void executeInServer() {
        // TODO
        // starting point for implementing the IC3 predicate generation
        // if you want to specify program options specific to this plugin,
        // please take a look at the existing plugins, e.g. the one for RDDL
    	setupEnv();

//    	ConstraintSolverSMT.testZ3(this.modelChecker.getContextExpression());
    	//Z3SMT.test_opt();
    	//Z3SMT.testLP();
    	//Z3SMT.testOptimize();
    	//Z3SMT.testExists();
    	Model model = modelChecker.getModel();
    	
    	if(model instanceof ModelPRISM) {
    		System.out.println("instance of ModelPRISM");
    	}
    	System.out.println("Procedure starts...");
    	ModelPRISM prismModel = (ModelPRISM)model;
    	assert model instanceof ModelPRISM : "not instance of ModelPRISM";
    	
        StringBuilder builder = new StringBuilder();
        Semantics semanticsType = prismModel.getSemantics();
        assert semanticsType != null;
        if (semanticsType.isCTMC()) {
            builder.append("ctmc");            
        } else if (semanticsType.isDTMC()) {
            builder.append("dtmc");
        } else if (semanticsType.isMDP()) {
            builder.append("mdp");
        } else {
            assert false;
        }
        
        PredModel predModel = new PredModel(prismModel);
        try {
			predModel.buildPredModel();
		} catch (EPMCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(prismModel);
        System.out.println(predModel);
        
        Properties props = prismModel.getPropertyList();
    	
        for(Entry<RawProperty, Expression> entry: props.getProperties().entrySet()) {
        	Expression expr = entry.getValue();
        	System.out.println("property: " + expr);
        	System.out.println("property: " + canHandle(expr));
        	System.out.println("property: " + expr.getCondition());
        	System.out.println("property: " + expr.getCompare()); // 0 
//        	System.out.println("property: " + expr.getCompareType().asExOpType(context.getContextValue()));
            TSModel tsModel = null;
			try {
				tsModel = new PrismModel(predModel,  getSafeProperty(expr));
			} catch (EPMCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    
            IC3 ic3 = new IC3(tsModel);
            boolean rv = ic3.check(2, true, false);
            tsModel.close();
            if(rv) {
            	System.out.println("SAFE...");
            }else {
            	System.out.println("UNSAFE...");
            }
        }

    }
    
    // Pmax<0 [true U p]
    private boolean canHandle(Expression expression) {
//    	assert expression.isQuantifier() && (this.modelChecker.getModel().isDTMC() || this.modelChecker.getModel().isMDP()) 
//    	: " invalid property for ic3 algorithm";
//    	if(! (! this.modelChecker.getModel().isMDP() || expression.isDirMax())) return false;
//		if(! (expression.getCompareType() == CmpType.LT || expression.getCompareType() == CmpType.IS)) return false;
//		
//		Expression zero = context.getZero();
//		if(! zero.equals(expression.getCompare())) return false; 
		return true;
    }
    
    private Expression getSafeProperty(Expression expression) {
    	assert canHandle(expression) ;
    	Expression untilExpr = expression.getQuantified();
    	if(untilExpr.isUntil()) {
    	  return untilExpr.getOperand2();
    	}else {
    		System.out.println("Op: " + untilExpr.getOperand1());
    		return untilExpr.getOperand1();
    	}
    }

}
