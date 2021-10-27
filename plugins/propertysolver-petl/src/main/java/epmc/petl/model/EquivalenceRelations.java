package epmc.petl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.expression.Expression;

public class EquivalenceRelations {
	private Map<String, List<Expression>> equivalenceRelations;
	
	public EquivalenceRelations() {
		equivalenceRelations = new HashMap<String, List<Expression>>();
	}
	
	public EquivalenceRelations(Map<String, List<Expression>> equivalenceRelations) {
		this.equivalenceRelations  = equivalenceRelations;
	}

	public Map<String, List<Expression>> getEquivalenceRelations() {
		return equivalenceRelations;
	}
	
	public void addRelation(String moduleName, Expression exp) {
		List<Expression> expList = equivalenceRelations.get(moduleName);
		if(expList == null)
		{
			expList = new ArrayList<Expression>();
			equivalenceRelations.put(moduleName, expList);
		}
		
		expList.add(exp);
	}
	public Set<Expression> getAllExpressions()
	{
		Set<Expression> res = new HashSet<Expression>();
		for(List<Expression> list : equivalenceRelations.values())
		{
			for(Expression exp : list)
			{
				res.add(exp);
			}
		}
		
		return res;
	}
	
	@Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Equivalence Relations:\n");
        for (Entry<String, List<Expression>> entry : equivalenceRelations.entrySet()) {
        	builder.append("  " + entry.getKey() + "\n");
        	List<Expression> expList = entry.getValue();
        	for(Expression exp : expList)
        	{
        		builder.append("  " + exp.toString() + "\n");
        	}
        }

        return builder.toString();
    }
}
