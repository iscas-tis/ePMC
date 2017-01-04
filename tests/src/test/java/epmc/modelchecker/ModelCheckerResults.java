package epmc.modelchecker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.modelchecker.ModelCheckerResult;
import epmc.modelchecker.RawProperty;


//TODO in case server functionality is indeed moved to JANI interaction plugin,
//this class might be deleted

// TODO continue documentation

/**
 * Class collecting all results of a model checking run.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelCheckerResults {
    /** String "Results:\n". */
    private final static String RESULTS = "Results:\n";
    private final static String TWOSPACE = "  ";
    private final static String COLONSPACE = ": ";
    private final static String ENDLINE = "\n";
    private Object commonResult;
    private final Map<RawProperty,Object> results = new LinkedHashMap<>();
    private final List<Object> resultList = new ArrayList<>();
    private final List<Object> publicResultList =
            Collections.unmodifiableList(resultList);
    
    public void set(ModelCheckerResult result) {
        assert result != null;
        if (result.getProperty() == null) {
            this.commonResult = result.getResult();
        } else {
            results.put(result.getProperty(), result.getResult());
            resultList.add(result.getResult());
        }
    }
    
    public void set(Object commonResult) {
        assert commonResult != null;
        this.commonResult = commonResult;
    }
    
    public Object getCommonResult() {
        return commonResult;
    }

    public String getCommonResultString() {
        if (commonResult == null) {
            return null;
        }
        return commonResult.toString();
    }

    public void add(RawProperty property, Object result) {
        assert property != null;
        assert result != null;
        results.put(property, result);
        resultList.add(result);
    }

    public Object get(RawProperty property) {
        assert property != null;
        return results.get(property);
    }
    
    public String getString(RawProperty property) {
        assert property != null;
        Object get = get(property);
        if (get == null) {
            return null;
        }
        return get.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(RESULTS);
        for (Entry<RawProperty,Object> entry : results.entrySet()) {
            builder.append(TWOSPACE);
            builder.append(entry.getKey().getDefinition());
            builder.append(COLONSPACE);
            builder.append(entry.getValue());
            builder.append(ENDLINE);
        }
        return builder.toString();
    }
    
    public List<Object> getResultList() {
        return publicResultList;
    }

    public Collection<RawProperty> getProperties() {
        return results.keySet();
    }
    
    public void clear() {
        this.commonResult = null;
        this.resultList.clear();
        this.results.clear();
    }
}
