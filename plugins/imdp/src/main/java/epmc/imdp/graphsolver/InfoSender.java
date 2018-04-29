package epmc.imdp.graphsolver;

import epmc.imdp.messages.MessagesIMDPGraphsolver;
import epmc.modelchecker.Log;
import epmc.util.RunningInfo.SendInformation;

final class InfoSender implements SendInformation {
    private long numIterations;
    private double difference;
    private long numOpt;
    private long numSort;
    private long time;
    private Log log;

    void setNumIterations(long numIterations) {
        this.numIterations = numIterations;
    }
    
    void setDifference(double difference) {
        this.difference = difference;
    }
    
    void setNumOpt(long numOpt) {
        this.numOpt = numOpt;
    }
    
    void setNumSort(long numSort) {
        this.numSort = numSort;
    }
    
    void setTimePassed(long time) {
        this.time = time;
    }

    void setLog(Log log) {
        this.log = log;
    }
    
    @Override
    public void call() {
        log.send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_PROGRESS,
                numIterations, Double.toString(difference),
                numOpt, numSort,
                Math.round(time / 1000.0));
    }
    
}