/*
 * aiSee.h
 *
 *  Created on: Aug 28, 2009
 *      Author: bwachter
 */

#ifndef AISEE_H_
#define AISEE_H_

namespace util {

void aiSeeNode(std::ostream &stream, std::string id, const std::string& label);
void aiSeeNode(std::ostream &stream, std::string id, const std::string& label,
		const std::string& infobox, const std::string& infobox2, int width,
		int height, const std::string& shape, const std::string& fill,
		int vertical_order);
void aiSeeEdge(std::ostream &stream, std::string source, std::string target,
		const std::string& label, int width = 0,
		const std::string& fill = "", const std::string& arrowstyle = "");
}

#endif /* AISEE_H_ */
