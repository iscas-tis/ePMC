package epmc.petl.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import epmc.expression.Expression;
import epmc.modelchecker.Property;
import epmc.modelchecker.RawProperties;
import epmc.petl.property.PETLExpressionParser;
import epmc.prism.model.PropertyPRISM;
import epmc.value.Type;

public class PropertyPETL implements Property{
	public final static String IDENTIFIER = "petl";
	
	PropertyPRISM propertyPrism = new PropertyPRISM();

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression parseExpression(Object identifier, InputStream stream) {
		assert stream != null;
        String string = null;
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
            string = buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        PETLExpressionParser parser = new PETLExpressionParser(stream);
        
        return parser.parseExpressionAsProperty(1, 1, string);
	}

	@Override
	public void readProperties(Object identifier, RawProperties properties, InputStream stream) {
		propertyPrism.readProperties(identifier, properties, stream);
	}

	@Override
	public void writeProperties(RawProperties properties, OutputStream stream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Type parseType(Object identifier, String type) {
		// TODO Auto-generated method stub
		return null;
	}
}
