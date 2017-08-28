/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.graph.explicit;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;

import epmc.value.Value;

public final class GraphExporterDOT {    
    public static void export(GraphExplicit graph, OutputStream stream) {
        PrintStream out = new PrintStream(stream);
        out.println("digraph {");
        Set<Object> nodeProperties = graph.getNodeProperties();
        Set<Object> edgeProperties = graph.getEdgeProperties();
        for (int node = 0; node < graph.getNumNodes(); node++) {
            out.print("  " + node + " [label=\"");
            int propNr = 0;
            for (Object property : nodeProperties) {
                Value value = graph.getNodeProperty(property).get(node);
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
            int numSucc = graph.getNumSuccessors(node);
            for (int succNr = 0; succNr < numSucc; succNr++) {
                int succ = graph.getSuccessorNode(node, succNr);
                out.print("  " + node + " -> " + succ + " [label=\"");
                int propNr = 0;
                for (Object property : edgeProperties) {
                    EdgeProperty prop = graph.getEdgeProperty(property);
                    Value value = prop.get(node, succNr);
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
        export(graph, out);
        return out.toString();
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private GraphExporterDOT() {
    }
}
