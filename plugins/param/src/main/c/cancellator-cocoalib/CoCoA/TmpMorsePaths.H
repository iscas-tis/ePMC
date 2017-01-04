#ifndef CoCoA_TmpMorsePaths_H
#define CoCoA_TmpMorsePaths_H

//   Copyright (c)  2013 Mario Albert

//   This file is part of the source of CoCoALib, the CoCoA Library.

//   CoCoALib is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 3 of the License, or
//   (at your option) any later version.

//   CoCoALib is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.

//   You should have received a copy of the GNU General Public License
//   along with CoCoALib.  If not, see <http://www.gnu.org/licenses/>.
//   -------------------------------------------------------------------------

#include "CoCoA/TmpMorseElement.H"
#include <map>


namespace CoCoA
{

  class MorsePaths;

  typedef std::map<MorseElement, MorsePaths>::const_iterator ConstResIter;

  struct MorseIterCompare
  {
    bool operator()(const ConstResIter& lhs, const ConstResIter& rhs) const;
  };

  typedef std::map<ConstResIter, RingElem, MorseIterCompare> PathMap;

  class MorsePaths
  {
  private:
    //basic datastructure
    PathMap myPaths;

  public:
    MorsePaths(): myPaths() {}

    // Return all paths.
    inline const PathMap& myGetPaths() const { return myPaths; }

    // Add to the path with origin m the RingElem elem.
    // If there is no path from m, we add a new path.
    void myAddPath(const ConstResIter& m, const RingElem& elem);

    // Test if there are any paths.
    inline bool IamEmpty() const { return myPaths.empty(); }

    // Return the path from MorseElement m.
    // WARNING!! This function does not work if we are in the betti case!!!
    inline ConstRefRingElem myGetPath(const ConstResIter& m)
    {
      const PathMap::iterator iter(myPaths.find(m));
      if (iter == myPaths.end())
      {
        return zero(owner((m->first).myGetBasisElement()));
      }
      return iter->second;
    }
  };


} // end of namespace CoCoA

#endif
