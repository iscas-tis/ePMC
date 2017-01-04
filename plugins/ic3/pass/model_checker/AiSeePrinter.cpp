#include "util/Util.h"
#include "util/Database.h"
#include "AiSeePrinter.h"
#include "GameGraph.h"

using namespace std;

namespace model_checker {

  /**
 * Print graph node with given attributes.
 */
inline
void printAiSeeNode(ostream &stream, int id, const std::string& label,
		const std::string& infobox, const std::string& infobox2, int width,
		int height, const std::string& shape, const std::string& fill) {
	stream << "node: { " << "\n" << "  title: \"" << id << "\"\n"
			<< "  width: " << width << "\n" << "  height: " << height << "\n"
			<< "  shape: " << shape << "\n";
	if ("" != infobox) {
		stream << "  info1: \"" << infobox << "\"\n";
	}
	if ("" != infobox2) {
		stream << "  info2: \"" << infobox2 << "\"\n";
	}
	if ("" != fill) {
		stream << "  color: " << fill << endl;
	}
	stream << "  label: \"" << label << "\"\n" << "}\n";
}

/**
 * Prints graph edge with given attributes.
 */
inline
void printAiSeeEdge(ostream &stream, int source, int target,
		const std::string& label, int width,
		// "line", "dashed", "dotted"
		const std::string& style,
		//"delta", "standard", "diamond", "short", "white_delta", "white_diamond", or "none"
		const std::string& sourceArrow,
		//"delta", "standard", "diamond", "short", "white_delta", "white_diamond", or "none"
		const std::string& targetArrow, const std::string& fill) {
	stream << "edge: {\n " << "  sourcename: \"" << source << "\"\n"
			<< "  targetname: \"" << target << "\"\n" << "  thickness: "
			<< width << "\n";
	if ("" != style) {
		stream << "  style: " << style << "\n";
	}
	if ("" != fill) {
		stream << "  color: " << fill << endl;
	}
	stream << "  backarrowstyle: " << sourceArrow << "\n" << "  arrowstyle: "
			<< targetArrow << "\n" << "  label: \"" << label << "\"\n"
			<< "  class: 1}\n";
}

void AiSeePrinter::printRegions(ostream &stream) {
	MSG(0,"AiSeePrinter::printRegions\n");
	for (std::vector<StrongComponent>::const_iterator i = graph.sccs.begin(); i
			!= graph.sccs.end(); ++i) {
		const StrongComponent& scc(*i);
		stream << "region: { source: \"" << (int) scc.root << "\" ";
		if (scc.target.size() > 0) {
			stream << " target: ";
			for (StateSet::const_iterator i = scc.target.begin(); i
					!= scc.target.end(); ++i) {
				stream << "\"" << (int) *i << "\" ";
			}
		}
		stream << "class: 1}\n";
	}
}

/**
 * Print game graph to aiSee format.
 */
AiSeePrinter::AiSeePrinter(const GameGraph &_graph, ostream &stream) :
	graph(_graph) {
	stream << "graph: {" << endl << endl << "colorentry 1: 153 255 0" << endl
			<< "colorentry 2: 255 255 0" << endl << "colorentry 3: 0 0 0"
			<< endl // black
			<< "colorentry 4: 255 255 221" << endl << "colorentry 5: 128 0 128"
			<< endl // purple
			<< "colorentry 6: 0 0 255" << endl // blue
			<< "colorentry 7: 255 0 0" << endl << endl; // red

	/* output node descriptions */
	for (vertex_iter_pair vp(vertices(graph.graph)); vp.first != vp.second; ++vp.first) {
		vertex_descriptor v = *vp.first;
		switch (graph.graph[v].vertexType) {
		case S: {

			bool init(graph.graph[v].stateProp.isInit());
			bool bad(graph.graph[v].stateProp.isBad());
			double max(graph.getProbability(v, false)), min(
					graph.getProbability(v, true));
			std::string label;
			int width = 40;
			int height = 40;
			std::string color;
			std::string shape(v == graph.pivotState ? "hexagon" : "box");

			if (init) {
				width = 80;
				height = 80;
				color = "1";
			} else if (bad) {
				width = 80;
				height = 80;
				color = "2";
			} else {
				color = (v == graph.pivotState ? "7" : "");

			}

			if (min == max) {
				label += util::floatToString(min);
			} else {
				label += "[" + util::floatToString(min) + ","
						+ util::floatToString(max) + "]";
			}

			std::unordered_map<State, std::string>::const_iterator it(graph.annotation.find(v));
			printAiSeeNode(stream, (int) v, label,
					it != graph.annotation.end() ? it->second : "",
					"SCC: " + util::intToString(graph.graph[v].scc_index),
					label.size() * 10, height, shape, color);
		}
			break;
		case P1: {

			std::string color = graph.loop_decision.count(v) ? "2" : "3";
			printAiSeeNode(stream, (int) v, "", "", "", 10, 10, "rhomb", color);
		}
			break;
		default: {

			std::unordered_map<State, std::string>::const_iterator it(
					graph.annotation.find(v));
			int action(graph.graph[v].p2Prop.action);
			std::string label(util::intToString(action));

			label = v == graph.pivotDistr ? "pivot" + label : label;

			printAiSeeNode(stream, (int) v, label,
					it != graph.annotation.end() ? it->second : "", "",
					label.size() * 10, 10, "triangle", "4");
		}
		}
	}

	/* output edges of graph, mark choices of graph */
	for (vertex_iter_pair vp = vertices(graph.graph); vp.first != vp.second; ++vp.first) {
		vertex_descriptor v = *vp.first;
		vertex_descriptor u;
		edge_descriptor e;
		int edge_nr = 0;
		for (out_edge_iter_pair it(out_edges(v, graph.graph)); it.first
				!= it.second; ++it.first) {
			e = *it.first;
			u = target(e, graph.graph);
			/* output "from" state */

			std::string label;
			int width;
			std::string style;
			std::string sourceArrow;
			std::string targetArrow;
			std::string fill;

			switch (graph.graph[v].vertexType) {
			case S:
			case P1: {
				edge_descriptor min_choice(graph.graph[v].P1min_P_choice);
				edge_descriptor max_choice(graph.graph[v].P1max_P_choice);

				bool is_min = (e == min_choice);
				bool is_max = (e == max_choice);

				sourceArrow = targetArrow = "none";

				if (is_min && is_max) {
					fill = "5"; // purple
					width = 3;
				} else if (is_min) {
					fill = "6"; // blue
					width = 2;
				} else if (is_max) {
					fill = "7"; // red
					width = 2;
				} else {
					fill = "3"; // black
					width = 1;
				}
			}
				break;
			case P2:
				sourceArrow = "none";
				targetArrow = "solid";
				label = util::floatToString(graph.graph[e]);
				width = 1;
				fill = "3";
				break;
			}

			printAiSeeEdge(stream, (int) v, (int) u, label, width, style,
					sourceArrow, targetArrow, fill);

			edge_nr++;
		}
	}

	//printRegions(stream);

	stream << "}" << endl;
}
}

