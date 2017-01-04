package epmc.modelchecker;

import java.io.InputStream;
import java.util.List;

import epmc.error.EPMCException;
import epmc.expression.Expression;

// TODO documentation

public interface Properties {
    void parseProperties(InputStream... inputs) throws EPMCException;
    
    List<RawProperty> getRawProperties();
    
    Expression getParsedProperty(RawProperty property);
}
