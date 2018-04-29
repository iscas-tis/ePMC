package epmc.imdp.graphsolver;

@FunctionalInterface interface Diff {
    double diff(double value1, double value2);
}
