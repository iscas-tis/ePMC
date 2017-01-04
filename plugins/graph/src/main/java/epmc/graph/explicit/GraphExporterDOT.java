package epmc.graph.explicit;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.value.Value;

public final class GraphExporterDOT {    
    public static void export(GraphExplicit graph, OutputStream stream) throws EPMCException {
        PrintStream out = new PrintStream(stream);
        out.println("digraph {");
        Set<Object> nodeProperties = graph.getNodeProperties();
        Set<Object> edgeProperties = graph.getEdgeProperties();
        for (int node = 0; node < graph.getNumNodes(); node++) {
            graph.queryNode(node);
            out.print("  " + node + " [label=\"");
            int propNr = 0;
            for (Object property : nodeProperties) {
                Value value = graph.getNodeProperty(property).get();
                out.print(property + "=" + value);
                if (propNr < nodeProperties.size() - 1) {
                    out.print(",");
                }
                propNr++;
            }
            out.println("\"];");
        }
        out.println();
        for (int node = 0; node < graph.getNumNodes(); node++) {
            graph.queryNode(node);
            int numSucc = graph.getNumSuccessors();
            for (int succNr = 0; succNr < numSucc; succNr++) {
                int succ = graph.getSuccessorNode(succNr);
                out.print("  " + node + " -> " + succ + " [label=\"");
                int propNr = 0;
                for (Object property : edgeProperties) {
                    EdgeProperty prop = graph.getEdgeProperty(property);
                    Value value = prop.get(succNr);
                    out.print(property + "=" + value);
                    if (propNr < edgeProperties.size() - 1) {
                        out.print(",");
                    }
                    propNr++;
                }
                out.println("\"];");
            }
        }

        out.println("}");
    }
    
    public static String toString(GraphExplicit graph) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            export(graph, out);
            return out.toString();
        } catch (EPMCException e) {
            return "ERROR";
        }
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private GraphExporterDOT() {
    }
}
