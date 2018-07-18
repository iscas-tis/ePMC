package epmc.param.points;

public interface PointResultsExporter {
    interface Builder {
        Builder setPointResults(PointResults results);
        
        PointResultsExporter build();
    }

    void export(Appendable result);
}
