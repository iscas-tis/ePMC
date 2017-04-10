package epmc.multiobjective;

import java.io.OutputStream;
import java.io.PrintStream;

import epmc.graph.LowLevel;
import epmc.graph.Scheduler;
import epmc.graph.SchedulerPrinter;
// import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.SchedulerSimple;
import epmc.options.Options;

public final class SchedulerPrinterSimple implements SchedulerPrinter {
	public final static String IDENTIFIER = "simple";

	private LowLevel lowLevel;
	private Scheduler scheduler;
	private Options options;
	private OutputStream out;

	@Override
	public void setLowLevel(LowLevel lowLevel) {
		this.lowLevel = lowLevel;
	}

	@Override
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void setOptions(Options options) {
		this.options = options;
	}

	@Override
	public void setOutput(OutputStream out) {
		this.out = out;
	}

	@Override
	public boolean canHandle() {
//		if (!(graph instanceof GraphExplicit)) {
	//		return false;
		//}
		if (!(scheduler instanceof SchedulerSimple)) {
			return false;
		}
		return true;
	}

	@Override
	public void print() {
		assert out != null;
//		assert graph != null;
		assert scheduler != null;
//		GraphExplicit graph = (GraphExplicit) lowLevel;
		SchedulerSimple schedulerSimple = (SchedulerSimple) scheduler;
		PrintStream printStream = new PrintStream(out);
		printStream.println("s 	 a");
		printStream.println("- 	 -");
		for (int state = 0; state < schedulerSimple.size(); state++) {
			int decision = schedulerSimple.getDecision(state);
			printStream.println((state + 1) + " 	 " + (decision + 1));				
		}
		printStream.println();
	}
}
