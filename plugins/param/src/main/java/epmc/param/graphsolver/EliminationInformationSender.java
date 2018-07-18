package epmc.param.graphsolver;

import epmc.graphsolver.OptionsGraphsolver;
import epmc.options.Options;
import epmc.util.RunningInfo;
import epmc.util.StopWatch;

final class EliminationInformationSender implements RunningInfo.SendInformation {
    private int statesInitially;
    private int statesDone;
    private int lastStatesDone;
    private long sleepTime = 1l;
    private int numTransitions;
    private StopWatch since = new StopWatch(true);
    
    public EliminationInformationSender(int statesInitially) {
        this.statesInitially = statesInitially;
        sleepTime = Options.get().getLong(OptionsGraphsolver.GRAPHSOLVER_UPDATE_DELAY);
    }

    void setStatesDone(int statesDone) {
        this.statesDone = statesDone;
    }

    void setNumTransitions(int numTransitions) {
        this.numTransitions = numTransitions;
    }
    
    @Override
    public void call() {
        double percentDone = statesDone / ((double) statesInitially);
        double speed = (statesDone - lastStatesDone) / (sleepTime / 1000.0);
        GraphSolverEliminator.getLog().send(MessagesParamGraphSolver.PARAM_ELIMINATION_PROGRESS,
                statesDone, statesInitially, percentDone, speed,
                numTransitions, Math.round(since.getTimeSeconds()));
        lastStatesDone = statesDone;
    }
}
