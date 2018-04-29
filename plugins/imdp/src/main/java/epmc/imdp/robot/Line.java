package epmc.imdp.robot;

import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;

final class Line implements Cloneable {
    private int from;
    private int action;
    private int to;
    private final ValueInterval interval;
    private final ValueReal reward1;
    private final ValueReal reward2;

    Line() {
        TypeInterval typeInterval = TypeInterval.get();
        TypeReal typeReal = TypeReal.get();
        interval = typeInterval.newValue();
        reward1 = typeReal.newValue();
        reward2 = typeReal.newValue();
    }

    int getFrom() {
        return from;
    }

    void setFrom(int from) {
        this.from = from;
    }

    int getAction() {
        return action;
    }

    void setAction(int action) {
        this.action = action;
    }

    int getTo() {
        return to;
    }

    void setTo(int to) {
        this.to = to;
    }

    ValueInterval getInterval() {
        return interval;
    }

    ValueReal getReward1() {
        return reward1;
    }

    ValueReal getReward2() {
        return reward2;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(from);
        builder.append(ModelIMDPRobot.SPACE);
        builder.append(action);
        builder.append(ModelIMDPRobot.SPACE);
        builder.append(to);
        builder.append(ModelIMDPRobot.SPACE);
        builder.append(interval);
        builder.append(ModelIMDPRobot.SPACE);
        builder.append(reward1);
        builder.append(ModelIMDPRobot.SPACE);
        builder.append(reward2);
        return builder.toString();
    }

    @Override
    public Line clone() {
        Line result = new Line();
        result.setFrom(from);
        result.setAction(action);
        result.setTo(to);
        OperatorEvaluator setInterval = ContextValue.get().getEvaluator(OperatorSet.SET, TypeInterval.get(), TypeInterval.get());
        OperatorEvaluator setReal = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        setInterval.apply(result.getInterval(), interval);
        setReal.apply(result.getReward1(), reward1);
        setReal.apply(result.getReward2(), reward2);
        return result;
    }
}