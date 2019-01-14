package epmc.param.value;

import epmc.value.TypeAlgebra;
import epmc.value.ValueArrayAlgebra;

public interface FunctionEvaluator {
    interface Builder {
        Builder addFunction(ValueFunction function);
        
        Builder setPointsUseIntervals(boolean useIntervals);
        
        Builder setResultType(TypeAlgebra type);

//        Builder setInput(Value input);
        
//       Builder setOutput(Value output);
        
        FunctionEvaluator build();
    }

    TypeAlgebra getResultType();
    
    int getResultDimensions();
    
    void evaluate(ValueArrayAlgebra result, ValueArrayAlgebra point);
    
//    boolean canPut();
    
//    void put();
    
 //   void take();

}
