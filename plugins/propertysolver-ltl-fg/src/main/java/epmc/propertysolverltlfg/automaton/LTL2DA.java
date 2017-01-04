package epmc.propertysolverltlfg.automaton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import epmc.automaton.HOAFormatParser;
import epmc.automaton.UtilAutomaton;
import epmc.automaton.part.PartHOAParser;
import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.propertysolverltlfg.UtilLTL;
import epmc.propertysolverltlfg.hoa.HOA2DFA;
import epmc.propertysolverltlfg.hoa.HOAParser;
import epmc.propertysolverltlfg.hoa.HOAUser;
import epmc.propertysolverltlfg.hoa.HOAUserPrinter;
import epmc.value.ContextValue;

/**
 * Take an expression as input as well as specify the automaton type,
 * we can further extend this class to use other automaton construction
 * tools which output automata in Hanoi format
 * @author Yong Li
 * */
public class LTL2DA {
	
	private static final String HOASTART = "HOA:";
	private static final String HOAEND = "--END--";
	// need to add option to set tool, but right now it is OK
	private static final String CAP_EXE = 
			"/home/liyong/workspace2/iscasmc/plugins/propertysolver-ltl-fg/lib/cap.jar";
	private static final String SPICY_EXE = 
			"/home/liyong/workspace2/iscasmc/plugins/propertysolver-ltl-fg/lib/spicy.jar";
	public static AutomatonRabin convertLTL2DA(ContextValue contextValue, Expression expression
			, AutomatonType type, boolean optimize, boolean verbose
			) throws EPMCException {
        assert expression != null;
        assert type != null;
        boolean isFDA = false;
        
        Map<Expression,String> expr2str = new HashMap<>();
        expression = UtilAutomaton.bounded2next(contextValue, expression);
        Set<Expression> apSet = UtilLTL.collectLTLInner(expression);
        int numAps = 0;
        for(Expression ap : apSet) {
        	expr2str.put(ap, "p" + numAps);
        	++ numAps; 
        }
        String expr = UtilExpressionStandard.expr2str(expression, expr2str);
        assert expr != null;
        LinkedHashMap<String,Expression> ap2expr = new LinkedHashMap<>();
        for (Entry<Expression,String> entry : expr2str.entrySet()) {
            ap2expr.put(entry.getValue(), entry.getKey());
        }
        //now we have ap2expr, start to construct automaton
		String autoType = "-auto=";
        switch (type)
        {
        case GRA: 
            autoType += "sgr";
            break;
        case TRA: 
            autoType += "tr";
            break;
        case RA: 
            autoType += "sr";
            break;
        case STGRA:
        	autoType += "fgr";
        	break;
        case TGRA:
            autoType += "tgr";
            break;
        case FDA:
        	isFDA = true;
            break;
        default:
        	assert false : "Unsupported automata type";
        }
        String opt = "";
        if(! optimize) opt = "-how=isabelle";
        else opt = "-how=optimize";
        
		String[] autExecArgs = null;
		if(isFDA){
			autExecArgs = new String[]{"java", "-jar"
					, CAP_EXE,
					"-format=hoa", "-out=std", expr };
		}else{
			autExecArgs = new String[]{ "java", "-jar"
				, SPICY_EXE,
				"-format=hoa", "-out=std", autoType, opt, expr };
		}
		Process autProcess = null;
		StringBuilder sBuilder = new StringBuilder();
		
		// extract Hanoi string
		try {
			autProcess = Runtime.getRuntime().exec(autExecArgs);
			final BufferedReader autIn = new BufferedReader(new InputStreamReader(
					autProcess.getInputStream()));
	        
			String line = null;
			boolean start = false, result = false;

			while ((line = autIn.readLine()) != null) {
				if(line.contains(HOASTART)) {
					start = result = true;
				}
				
				if(start) {
					sBuilder.append(line + "\n");
					if(line.contains(HOAEND)) start = false;
				}
			}
			
			if(! result) {
				System.err.println("Automaton available for " + expression);
				System.exit(-1);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(verbose) {
			System.out.println("Automaton for LTL " + expr + "\n");
			System.out.println(sBuilder.toString());
		}
		HOAParser parser = null;
		if(isFDA) {
			parser = new PartHOAParser(new StringReader(sBuilder.toString()));
		}
		else{
			parser = new HOAFormatParser(new StringReader(sBuilder.toString()));
		}
		HOAUser user = getHOAUserInstance(context, ap2expr, type);
		parser.parse(context, user);
		user.prepare();
        return user.getAutomaton();
	}
	
	public static HOAUser getHOAUserPrinter() {
		return new HOAUserPrinter();
	}	
	
	public static HOAUser getHOAUserInstance(ContextExpression context
			, Map<String, Expression> ap2expr
			, AutomatonType type) throws EPMCException {
		return new HOA2DFA(context, ap2expr, type);
	}
	

}
