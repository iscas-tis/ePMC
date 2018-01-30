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
    
    public void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }
    
    public void setDifference(double difference) {
        this.difference = difference;
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
                ? numIterationsMemory.getInt(0)
                : this.numIterations;
        Double difference = differenceMemory != null
                ? differenceMemory.getDouble(0)
                : this.difference;
        if (numIterations != null && difference != null) {
            log.send(MessagesGraphSolverIterative.ITERATING_PROGRESS,
                    numIterations, Double.toString(difference),
                    Math.round(watch.getTime() / 1000.0));
        }
    }
}
