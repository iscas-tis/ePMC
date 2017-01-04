package epmc.propertysolverltlfg.hoa;

import java.util.List;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.propertysolverltlfg.automaton.AcceptanceCondition;
import epmc.propertysolverltlfg.automaton.AutomatonRabin;
import epmc.propertysolverltlfg.automaton.AutomatonType;
import epmc.util.BitSet;
/**
 * Printer class for Hanoi format 
 * @author Yong Li
 * */
public class HOAUserPrinter implements HOAUser {

	
	public HOAUserPrinter() {
	}
	
	@Override
	public void parseStart() {
		try {
			System.out.print("\n--------- start parsing ---------\n");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setHOAVer(String version) {
		try {
			System.out.print("HOA: " + version + "\n");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setTool(String name, String version) {
		try {
			System.out.print("tool: " + name + " " + version + "\n");
		}catch(Exception e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void setAutName(String name) {
		try {
			System.out.print("name: " + name + "\n");
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void addProperties(List<String> properties) {
		try {
			System.out.print("properties:");
			for(String prop : properties) {
				System.out.print(" " + prop);
			}
			System.out.print("\n");
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void setNumOfStates(int numOfStates) {
		try {
			System.out.print("States: " + numOfStates + "\n");
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void setStartStates(List<Integer> conjStates) {
		try {
			System.out.print("Start: ");
			boolean flag = true;
			for(Integer state : conjStates) {
				if(! flag) System.out.print("&");
				flag = false;
				System.out.print(state);
			}
			System.out.print("\n");
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void setAccName(String name, List<Integer> nums) {
		try {
			System.out.print("acc-name: " + name);
			for(Integer state : nums) {
				System.out.print(" " + state);
			}
			System.out.print("\n");
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void setAcceptances(int numOfSets, List<AcceptanceCondition> accs) {
		if(accs == null ) return ;
		try {
			System.out.print("Acceptance: " + numOfSets + " ");
			boolean flag = true;
			for(AcceptanceCondition acc : accs) {
				if(! flag) System.out.print(" | ");
				flag = false;
				System.out.print(acc.toString());
			}
			System.out.print("\n");
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void setAps(int numOfAps, List<String> aps) {
		try {
			System.out.print("AP: " + numOfAps);
			for(String ap : aps) {
				System.out.print(" \"" + ap + "\"");
			}
			System.out.print("\n");
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void startBody() {
		try {
			System.out.print("--BODY--\n");
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void startOfState() {
		try {
			System.out.print("--------------\n");
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void setCurrState(int id, Expression label, String comment,
			BitSet signature) {
		try {
			System.out.print("State: ");
			if(label != null) {
				System.out.print("[");
				System.out.print(label.toString());
				System.out.print("] ");
			}
			System.out.print(id + " ");
			if(comment != null) {
				System.out.print("\"" + comment + "\"");
			}
			System.out.print("\n");
			if(signature != null) {
				System.out.print(signature + "\n");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void addEdge(int succ, Expression label, BitSet signature) {
		try {
			if(label != null) {
				System.out.print("[");
				System.out.print(label.toString());
				System.out.print("] ");
			}
			System.out.print(succ + " ");
			if(signature != null) {
				System.out.print(signature.toString());
			}
			System.out.print("\n");
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void endOfState() {
		try {
			System.out.print("--------------\n");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void endBody() {
		try {
			System.out.print("--END--\n");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void parseEnd() {
		try {
			System.out.print("--------end parsing -----------\n");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void abort() {
		try {
			System.out.print("------------ abort -----------\n");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public AutomatonType getAutomatonType() {
		return null;
	}

	@Override
	public AutomatonRabin getAutomaton() {
		return null;
	}

	@Override
	public void setAccExpressions(int numOfSets, List<Expression> accs) {
		if(accs == null) return ;
		try {
			System.out.print("Acceptance: " + numOfSets + " ");
			for(Expression acc : accs) {
				System.out.print(" " + acc.toString());
			}
			System.out.print("\n");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void prepare() throws EPMCException {
		// TODO Auto-generated method stub
		
	}

}
