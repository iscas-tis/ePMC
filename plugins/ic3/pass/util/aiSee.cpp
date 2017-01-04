/*
 * aiSee.cpp
 *
 *  Created on: Aug 28, 2009
 *      Author: bwachter
 */
#include "util/Util.h"

namespace util {

void aiSeeNode(std::ostream &stream, std::string id, const std::string& label) {
	stream << "node: { " << "\n" << "  title: \"" << id << "\"\n";
	stream << "  label: \"" << label << "\"\n";
	stream << "}\n";
}

/**
* Print graph node with given attributes.
*/

void aiSeeNode(std::ostream &stream, std::string id, const std::string& label,
		const std::string& infobox, const std::string& infobox2, int width,
		int height, const std::string& shape, const std::string& fill,
		int vertical_order) {
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
		stream << "  color: " << fill << "\n";
	}
	stream << "  label: \"" << label << "\"\n";
	stream << "  vertical_order: " << vertical_order <<"\n";
	stream << "}\n";

}

/**
* Prints graph edge with given attributes.
*/
void aiSeeEdge(std::ostream &stream, std::string source, std::string target,
		const std::string& label, int width,
		const std::string& fill, const std::string& arrowstyle) {
	stream << "edge: {\n " << "  sourcename: \"" << source << "\"\n"
			<< "  targetname: \"" << target << "\"\n" << "  thickness: "
			<< width << "\n";
	if ("" != fill)
		stream << "  color: " << fill << "\n";
	if( label != "")
		stream  << "  label: \"" << label << "\"\n";

	if(arrowstyle!="")
		stream  << "  arrowstyle: "<< arrowstyle <<"\n";

	stream << "}\n";
}

}


