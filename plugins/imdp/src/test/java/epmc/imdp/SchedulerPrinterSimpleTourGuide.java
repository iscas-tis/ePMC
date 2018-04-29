package epmc.imdp;

import java.io.OutputStream;
import java.io.PrintStream;

import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.graph.CommonProperties;
import epmc.graph.LowLevel;
import epmc.graph.Scheduler;
import epmc.graph.SchedulerPrinter;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
// import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.SchedulerSimple;
import epmc.jani.model.Action;

public final class SchedulerPrinterSimpleTourGuide implements SchedulerPrinter {
    public final static String IDENTIFIER = "simple";

    private LowLevel lowLevel;
    private Scheduler scheduler;
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
        SchedulerSimple schedulerSimple = (SchedulerSimple) scheduler;
        PrintStream printStream = new PrintStream(out);
        GraphExplicit graph = (GraphExplicit) lowLevel;
        NodeProperty xProp = graph.getNodeProperty(new ExpressionIdentifierStandard.Builder()
                .setName("x").build());
        assert xProp != null;
        NodeProperty yProp = graph.getNodeProperty(new ExpressionIdentifierStandard.Builder()
                .setName("y").build());
        assert yProp != null;
        EdgeProperty labelProp = graph.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
        int numStates = graph.computeNumStates();

        printStream.print("\\documentclass{article}\n");
        printStream.print("\\input{commands}\n");
        printStream.print("\\begin{document}\n");
        printStream.print("\\begin{tikzpicture}\n");
        for (int state = 0; state < numStates; state++) {
            int x = xProp.getInt(state);
            int y = yProp.getInt(state);
            int decision = schedulerSimple.getDecision(state);
            Action label = labelProp.getObject(state, decision);
            String labelStr = label.getName();
            if (!labelStr.equals("τ")) {
                printStream.print(String.format("\\placeArrow%s{%d}{%d}\n",labelStr, x, y));
            }
            //			printStream.println((state + 1) + " " + xProp.get(state) + " " + yProp.get(state) + " 	 " + (decision + 1) + "   " + labelStr);
        }
        printStream.print("\\end{tikzpicture}\n");
        printStream.print("\\end{document}\n");
        printStream.println();
    }
}
