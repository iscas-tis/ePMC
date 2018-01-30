package epmc.graphsolver.iterative;


import com.sun.jna.Memory;

import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.StopWatch;
import epmc.value.ValueReal;
import epmc.util.RunningInfo.SendInformation;

public final class Info implements SendInformation {
    private final StopWatch watch = new StopWatch(true);
    private final Log log = Options.get().get(OptionsMessages.LOG);
    private Integer numIterations;
    private Double difference;
    private Memory numIterationsMemory;
    private Memory differenceMemory;
    private Integer totalNumberIterations;
    
    public void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }
    
    public void setDifference(double difference) {
        this.difference = difference;
    }

    public void setTotalNumberIterations(int totalNumIterations) {
        this.totalNumberIterations = totalNumIterations;
    }
    
    public void setNumIterations(Memory numIterations) {
        this.numIterationsMemory = numIterations;
    }
    
    public void setDifference(Memory difference) {
        this.differenceMemory = difference;
    }
    
    public void setDifference(ValueReal distance) {
        this.difference = distance.getDouble();
    }

    public Memory createNumIterations() {
        numIterationsMemory = new Memory(Integer.BYTES);
        return numIterationsMemory;
    }
    
    public Memory createDifference() {
        differenceMemory = new Memory(Double.BYTES);
        return differenceMemory;
    }
    
    @Override
    public void call() {
        Integer numIterations = numIterationsMemory != null
                ? new Integer(numIterationsMemory.getInt(0))
                : this.numIterations;
        Double difference = differenceMemory != null
                ? new Double(differenceMemory.getDouble(0))
                : this.difference;
        if (numIterations != null && difference != null) {
            log.send(MessagesGraphSolverIterative.ITERATING_PROGRESS_UNBOUNDED,
                    numIterations, Double.toString(difference),
                    Math.round(watch.getTime() / 1000.0));
        } else if (numIterations != null && totalNumberIterations != null) {
            double percentDone = ((double) numIterations) / totalNumberIterations;
            log.send(MessagesGraphSolverIterative.ITERATING_PROGRESS_BOUNDED,
                    numIterations, totalNumberIterations,
                    percentDone, Math.round(watch.getTime() / 1000.0));
        }
    }
}
