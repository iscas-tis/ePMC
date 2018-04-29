package epmc.imdp.robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import epmc.graph.LowLevel;
import epmc.graph.Semantics;
import epmc.graph.SemanticsIMDP;
import epmc.modelchecker.Model;
import epmc.modelchecker.Properties;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;
import epmc.value.ValueSetString;
import epmc.modelchecker.PropertiesDummy;

public final class ModelIMDPRobot implements Model {
    public final static String IDENTIFIER = "imdp-robot";
    private final static String HEADER1 = "s 	 a 	 s' 	 P_lower 	 P_upper 	 rew1 	 rew2";
    private final static String HEADER2 = "- 	 - 	 --- 	 ------- 	 ------- 	 --- 	 ---";
    private final static String LINE_SPLITTER = "\\s+";
    final static String SPACE = " ";

    private final List<Line> lines = new ArrayList<>();
    private Properties properties;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void read(Object part, InputStream... inputs) {
        assert inputs != null;
        assert inputs.length == 1;
        for (InputStream input : inputs) {
            assert input != null;
        }
        TypeWeightTransition.set(TypeInterval.get());
        Line line = new Line();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputs[0]));

        lines.clear();
        readHeader(reader);
        try {
            for (String lineString = reader.readLine(); lineString != null; lineString = reader.readLine()) {
                readLine(line, lineString);
                lines.add(line.clone());
                //				System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        properties = new PropertiesDummy();
    }

    private void readHeader(BufferedReader reader) {
        assert reader != null;
        String header1;
        try {
            header1 = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert header1.equals(HEADER1);
        String header2;
        try {
            header2 = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert header2.equals(HEADER2);
    }

    private void readLine(Line line, String lineString) {
        assert line != null;
        assert lineString != null;
        String[] split = lineString.split(LINE_SPLITTER);
        line.setFrom(Integer.parseInt(split[0]) - 1);
        line.setAction(Integer.parseInt(split[1]) - 1);
        line.setTo(Integer.parseInt(split[2]) - 1);
        String lowerString = split[3];
        String upperString = split[4];
        String reward1String = split[5];
        String reward2String = split[6];
        ValueSetString.as(line.getInterval().getIntervalLower()).set(lowerString);
        ValueSetString.as(line.getInterval().getIntervalUpper()).set(upperString);
        ValueSetString.as(line.getReward1()).set(reward1String);
        ValueSetString.as(line.getReward2()).set(reward2String);
    }

    @Override
    public Semantics getSemantics() {
        return SemanticsIMDP.IMDP;
    }

    LowLevel newGraphExplicit(Set<Object> graphProperties, Set<Object> nodeProperties,
            Set<Object> edgeProperties) {
        GraphExplicitIMDPRobot graph = new GraphExplicitIMDPRobot();
        for (Line line : lines) {
            graph.addLine(line);
        }
        graph.done();
        return graph;
    }

    @Override
    public Properties getPropertyList() {
        return properties;
    }

}
