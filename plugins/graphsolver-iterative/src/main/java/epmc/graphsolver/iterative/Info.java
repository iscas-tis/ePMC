package epmc.graphsolver.iterative;


import com.sun.jna.Memory;

import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.StopWatch;
import epmc.util.RunningInfo.SendInformation;

public final class Info implements SendInformation {
    private final StopWatch watch = new StopWatch(true);
    private final Log log = Options.get().get(OptionsMessages.LOG);
    private int numIterations;
    private double difference;
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
        int numIterations = numIterationsMemory != null
                ? numIterationsMemory.getInt(0)
                : this.numIterations;
        double difference = differenceMemory != null
                ? differenceMemory.getDouble(0)
                : this.difference;
        log.send(MessagesGraphSolverIterative.ITERATING_PROGRESS,
                numIterations, Double.toString(difference),
                Math.round(watch.getTime() / 1000.0));
    }    
}
