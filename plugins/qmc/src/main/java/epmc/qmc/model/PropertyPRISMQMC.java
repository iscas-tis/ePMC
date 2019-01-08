package epmc.qmc.model;

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
import epmc.prism.model.PropertyPRISM;
import epmc.qmc.expression.QMCExpressionParser;
import epmc.value.Type;

public final class PropertyPRISMQMC implements Property {
    public final static String IDENTIFIER = "imdp";
    private final PropertyPRISM propertyPRISM = new PropertyPRISM();

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Expression parseExpression(Object part, InputStream stream) {
        assert stream != null;
        String string = null;
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
            string = buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        QMCExpressionParser parser = new QMCExpressionParser(stream);
        return parser.parseExpressionAsProperty(1, 1, string);
    }

    @Override
    public void writeProperties(RawProperties properties, OutputStream stream) {
        assert properties != null;
        assert stream != null;
        assert stream != null;
        propertyPRISM.writeProperties(properties, stream);
    }

    @Override
    public void readProperties(Object part, RawProperties properties, InputStream stream) {
        assert stream != null;
        propertyPRISM.readProperties(part, properties, stream);
    }

    @Override
    public Type parseType(Object part, String type) {
        return propertyPRISM.parseType(part, type);
    }
}
