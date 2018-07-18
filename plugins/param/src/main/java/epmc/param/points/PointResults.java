package epmc.param.points;

import epmc.param.value.ParameterSet;
import epmc.value.Type;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueArrayInterval;

public interface PointResults {
    Type getResultType();

    ParameterSet getParameters();

    boolean pointsIsIntervals();
    
    int getResultDimensions();

    boolean hasNext();
    
    void next(ValueArrayInterval point, ValueArrayAlgebra result);
}
