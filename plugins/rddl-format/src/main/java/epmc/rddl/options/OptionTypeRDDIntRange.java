package epmc.rddl.options;

import static epmc.error.UtilError.ensure;

import java.util.LinkedHashMap;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.options.OptionType;
import epmc.options.ProblemsOptions;

public final class OptionTypeRDDIntRange implements OptionType {
    
    @Override
    public Object parse(String value, Object prevValue) throws EPMCException {
        assert value != null;
        Map<String,RDDLIntRange> result;
        if (prevValue == null) {
            result = new LinkedHashMap<>();
        } else {
            result = uncheckedCast(prevValue);
        }
        String[] pairs = value.split(",");
        for (String pair : pairs) {
            String[] pairSplit = pair.split("=");
            ensure(pairSplit.length == 2, ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, value);
            String key = pairSplit[0].trim();
            RDDLIntRange entry = createIntRage(pairSplit[1].trim());
            ensure(!result.containsKey(key), ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, value);
            result.put(key, entry);
        }
        return result;
    }

    private RDDLIntRange createIntRage(String string) {
    	assert string != null;
    	string = string.trim();
    	assert string.length() >= 2;
    	assert string.charAt(0) == '[' : string;
    	assert string.charAt(string.length() - 1) == ']' : string;
    	string = string.substring(1, string.length() - 1);
    	String[] split = string.split("\\.\\.");
    	int lower = Integer.parseInt(split[0]);
    	int upper = Integer.parseInt(split[1]);
    	assert lower <= upper;
    	return new RDDLIntRange(lower, upper);
	}

	@SuppressWarnings("unchecked")
    private Map<String, RDDLIntRange> uncheckedCast(Object prevValue) {
        return (Map<String,RDDLIntRange>) prevValue;
    }

	@Override
    public String getInfo() {
        return "<name>=[l..u](,<name>=[l..u])*";
    }    
	
	@Override
	public String toString() {
		return getInfo();
	}
	
	@Override
	public Object getDefault() {
		return new LinkedHashMap<String,RDDLIntRange>();
	}
}
