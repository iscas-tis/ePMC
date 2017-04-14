package epmc.graph;

import java.io.OutputStream;

import epmc.error.EPMCException;
import epmc.options.Options;

public interface SchedulerPrinter {
	void setLowLevel(LowLevel lowLevel);
	
	void setScheduler(Scheduler scheduler);
	
	void setOptions(Options options);

	void setOutput(OutputStream out);

	boolean canHandle();
		
	void print() throws EPMCException;
}
