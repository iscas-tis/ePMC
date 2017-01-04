package epmc.jani.measure;

import epmc.value.Type;
import epmc.value.Value;

public interface DiscreteMeasure {
	static int NEG_INF = Integer.MIN_VALUE;
	static int POS_INF = Integer.MAX_VALUE;	

	Type getEntryType();
	
	void getTotal(Value total);
	
	int getFrom();
	
	int getTo();
	
	void getValue(Value value, int of);
}
