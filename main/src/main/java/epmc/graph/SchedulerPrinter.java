package epmc.graph;

import java.io.OutputStream;

import epmc.error.EPMCException;

public interface SchedulerPrinter {
	void setLowLevel(LowLevel lowLevel);
	
	void setScheduler(Scheduler scheduler);
	
	void setOutput(OutputStream out);

	boolean canHandle();
		
	void print() throws EPMCException;
}
