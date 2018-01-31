package epmc.graphsolver.iterative;

import epmc.graphsolver.OptionsGraphsolver;
import epmc.options.Options;
import epmc.util.RunningInfo;

public final class UtilGraphSolverIterative {
    @FunctionalInterface
    public interface Call <T> {
        T call(Info info);
    }

    @FunctionalInterface
    public interface CallVoid {
        void call(Info info);
    }
    
    public static void startWithInfoBoundedVoid(int bound, CallVoid call) {
        RunningInfo.startWithInfoVoid(running -> {
            running.setSleepTime(getSleepTime());
            Info info = new Info();
            info.setTotalNumberIterations(bound);
            running.setInformationSender(info);
            call.call(info);
        });
    }
    
    public static <T> T startWithInfoBounded(int bound, Call<T> call) {
        return RunningInfo.startWithInfo(running -> {
            running.setSleepTime(getSleepTime());
            Info info = new Info();
            info.setTotalNumberIterations(bound);
            running.setInformationSender(info);
            return call.call(info);
        });
    }

    public static void startWithInfoUnboundedVoid(CallVoid call) {
        RunningInfo.startWithInfoVoid(running -> {
            running.setSleepTime(getSleepTime());
            Info info = new Info();
            running.setInformationSender(info);
            call.call(info);
        });
    }
    
    public static <T> T startWithInfoUnbounded(Call<T> call) {
        return RunningInfo.startWithInfo(running -> {
            running.setSleepTime(getSleepTime());
            Info info = new Info();
            running.setInformationSender(info);
            return call.call(info);
        });
    }

    private static long getSleepTime() {
        return Options.get()
                .getLong(OptionsGraphsolver.GRAPHSOLVER_UPDATE_DELAY);
    }
    
    private UtilGraphSolverIterative() {
    }
}
