package epmc.multiobjective;

import java.io.OutputStream;
import java.io.PrintStream;

import epmc.graph.LowLevel;
import epmc.graph.Scheduler;
import epmc.graph.SchedulerPrinter;
import epmc.options.Options;
import epmc.util.Util;
import epmc.value.ValueAlgebra;

public final class SchedulerPrinterInitialRandomised implements SchedulerPrinter {
	public final static String IDENTIFIER = "initial-randomised";
	
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
		if (!(scheduler instanceof SchedulerInitialRandomised)) {
			return false;
		}
		return true;
	}

	@Override
	public void print() {
		assert scheduler != null;
		SchedulerInitialRandomised schedulerInitialRandomised = (SchedulerInitialRandomised) scheduler;
		PrintStream printStream = new PrintStream(out);
		for (int schedNr = 0; schedNr < schedulerInitialRandomised.size(); schedNr++) {
			ValueAlgebra schedProb = schedulerInitialRandomised.getProbability(schedNr);
			Scheduler schedSi = schedulerInitialRandomised.getScheduler(schedNr);
			printStream.println("probability: " + schedProb);
			Util.printScheduler(out, lowLevel, schedSi, options);
		}
		printStream.flush();
	}
}
