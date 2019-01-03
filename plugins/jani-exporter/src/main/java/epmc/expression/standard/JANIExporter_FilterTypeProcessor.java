/**
 * 
 */
package epmc.expression.standard;

import static epmc.error.UtilError.ensure;
import javax.json.JsonValue;

import epmc.jani.exporter.error.ProblemsJANIExporter;
import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.util.UtilJSON;

public class JANIExporter_FilterTypeProcessor implements JANIExporter_Processor {
    private static final String FUN_MIN = "min";
    private static final String FUN_MAX = "max";
    private static final String FUN_SUM = "sum";
    private static final String FUN_AVG = "avg";
    private static final String FUN_COUNT = "count";
    private static final String FUN_FORALL = "∀";
    private static final String FUN_EXIST = "∃";
    private static final String FUN_ARGMIN = "argmin";
    private static final String FUN_ARGMAX = "argmax";
    private static final String FUN_VALUES = "values";

    private FilterType filterType = null;
    
    @Override
    public JANIExporter_Processor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof FilterType;
   
        filterType = (FilterType) obj;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert filterType != null;
        
        String fun = null;
        switch (filterType) {
        case ARGMAX:
            fun = FUN_ARGMAX;
            break;
        case ARGMIN:
            fun = FUN_ARGMIN;
            break;
        case AVG:
            fun = FUN_AVG;
            break;
        case COUNT:
            fun = FUN_COUNT;
            break;
        case EXISTS:
            fun = FUN_EXIST;
            break;
        case FORALL:
            fun = FUN_FORALL;
            break;
        case MAX:
            fun = FUN_MAX;
            break;
        case MIN:
            fun = FUN_MIN;
            break;
        case SUM:
            fun = FUN_SUM;
            break;
        case PRINTALL:
            fun = FUN_VALUES;
            break;
        default:
            ensure(false, 
                    ProblemsJANIExporter.JANI_EXPORTER_ERROR_UNKNOWN_FILTERTYPE,
                    filterType);
            break;
        }
        
        return UtilJSON.toStringValue(fun);
    }
}
