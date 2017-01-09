package epmc.modelchecker;

import java.io.InputStream;
import java.io.OutputStream;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.value.ContextValue;
import epmc.value.Type;

/**
 * Class to handle properties of one particular input/output format.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Property {
    /**
     * Return unique identifier for the property type.
     * 
     * @return unique identifier of property type
     */
    String getIdentifier();
    
    /**
     * Sets the expression context used for this property type.
     * This method should be used before parsing the property using
     * the {@link #read(InputStream...) read} method. This is necessary because
     * all the expressions used in the same model checking process need to be
     * created using the same context. The same holds for the values and
     * decision diagrams. The context given must not be {@code null}.
     * 
     * @param context value context to be used for reading the model
     */
    void setContext(ContextValue context);
    
    /**
     * Get the value previously set by {@link #setContext(ContextExpression)}.
     * 
     * @return context previously set
     */
    ContextValue getContext();
    
    /**
     * Parse a property in the syntax of this property type.
     * An exception might be thrown if the input string is not a valid
     * expression in this format. Before calling this method, the expression
     * context must have been set using {@link #setContext(ContextExpression)
     * setContext}. The input parameter must not be <code>null</code>.
     * 
     * @param expression expression to be parsed.
     * @return parsed expression
     * @throws EPMCException parsing failed, e.g. due to syntax errors
     */
    Expression parseExpression(InputStream expression) throws EPMCException;

    /**
     * Parse a type in the syntax of this property type.
     * An exception might be thrown if the input string is not a valid
     * type in this format. Before calling this method, the expression
     * context must have been set using {@link #setContext(ContextExpression)
     * setContext}. The input parameter must not be {@code null}.
     * 
     * @param expression expression to be parsed.
     * @return parsed expression
     * @throws EPMCException parsing failed, e.g. due to syntax errors
     */
    Type parseType(String type) throws EPMCException;
    
    /**
     * Reads a complete list of properties in the format of this property type.
     * 
     * @param properties where to store properties
     * @param stream stream from which to read the properties
     * @throws EPMCException thrown in case of problems during reading
     */
    void readProperties(RawProperties properties, InputStream stream) throws EPMCException;
    
    /**
     * Writes a complete list of properties in the format of this property type.
     * This will only work if all properties are of the property type of this
     * class.
     * 
     * @param properties properties to be stored
     * @param stream stream to store properties to
     * @throws EPMCException thrown in case of problems during writing
     */
    void writeProperties(RawProperties properties, OutputStream stream) throws EPMCException;
}
