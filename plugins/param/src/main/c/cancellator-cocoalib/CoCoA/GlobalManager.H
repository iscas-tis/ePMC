#ifndef CoCoA_GlobalManager_H
#define CoCoA_GlobalManager_H

//   Copyright (c)  2007,2009-2011  John Abbott

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

#include "CoCoA/FractionField.H"
#include "CoCoA/MemPool.H"
#include "CoCoA/random.H"

#include <memory>
// using std::auto_ptr;

namespace CoCoA
{

  class DenseUPolyRing; // fwd decl -- defined in DenseUPolyRing.H

  // This class simply manages the parameters for the GlobalManager ctor.
  class GlobalSettings
  {
  public:
    enum ResidueSetting {SymmResidues, NonNegResidues};
    enum AllocatorSetting {GMPAllocator, SystemAllocator};

  public:
    GlobalSettings();
    GlobalSettings& mySetResidueSetting(ResidueSetting r);
    GlobalSettings& mySetAllocatorSetting(AllocatorSetting a);
    GlobalSettings& mySetSliceSize(std::size_t SliceSize);
    GlobalSettings operator()(std::size_t SliceSize) const; // NB creates a new object!!

  private: // data members
    bool myResidueSettingHasBeenSet;
    bool myAllocatorSettingHasBeenSet;
    bool mySliceSizeHasBeenSet;
    ResidueSetting myResidueSetting;
    AllocatorSetting myAllocatorSetting;
    std::size_t mySliceSize;
    static const std::size_t ourDefaultSliceSize;
    static const ResidueSetting ourDefaultResidueSetting;
    static const AllocatorSetting ourDefaultAllocatorSetting;

    friend GlobalSettings operator+(const GlobalSettings& arg1, const GlobalSettings& arg2);

    friend class GlobalManager;
  };

  GlobalSettings operator+(const GlobalSettings& arg1, const GlobalSettings& arg2);

  extern const GlobalSettings UseSymmResidues;
  extern const GlobalSettings UseNonNegResidues;
  extern const GlobalSettings UseSystemAllocatorForGMP;
  extern const GlobalSettings UseGMPAllocator;


  // At most one instance of this class may exist at any given time.
  class GlobalManager // : private BOOST::noncopyable ???
  {
  public:
    GlobalManager(const GlobalSettings& settings = GlobalSettings());
    ~GlobalManager();
    const ring& myRingZZ() const { return myZZQQMgr.myRingZZ; }
    const FractionField& myRingQQ() const { return myZZQQMgr.myRingQQ; }
    static const GlobalManager* ptr(const char* FnName);
  private: // disable copy ctor and assignment
    GlobalManager(const GlobalManager&);            ///< NEVER DEFINED -- disable copy ctor
    GlobalManager& operator=(const GlobalManager&); ///< NEVER DEFINED -- disable assignment
  private: // the true global variables
    static std::size_t GMPSliceSize;          // size in bytes of slices in the GMP MemPool
    static MemPool* GMPPoolPtr;               // raw ptr to GMP MemPool (or NULL if there is none)
    static const GlobalManager* ourGlobalDataPtr; ///< NULL or addr of unique existing GlobalManager

    friend class GMPMemMgr;  // so it can set GMPPoolPtr and GMPSliceSize.
    friend std::size_t GlobalGMPSliceSize();  // accessor fn
    friend MemPool* GlobalGMPPoolPtr();       // accessor fn
  private:
    friend const ring& RingZZ();              ///< defined in RingZZ.C
    friend const FractionField& RingQQ();     ///< defined in RingQQ.C
    friend GlobalSettings::ResidueSetting DefaultResidueSetting();     ///< called by ctors for rings of the form Z/(nZ)
    friend RandomSource& GlobalRandomSource();    
    //-- for Hilbert-Poincare' series --
    friend void MakeGlobalHPPowerList(const DenseUPolyRing& HSRing);
    friend int HPPowerListMaxDeg();
    friend ConstRefRingElem HPPowerList(int exp);
    void CopyHPPower(RingElem& res, int exp);
  private:
    class GMPMemMgr
    {
    public:
      GMPMemMgr(GlobalSettings::AllocatorSetting choice, std::size_t SliceSize);
      ~GMPMemMgr();
    private: // data members of GMPMemMgr
      std::auto_ptr<MemPool> myPoolPtr;
      void *(*myPrevAlloc) (std::size_t);
      void *(*myPrevRealloc) (void *, std::size_t, std::size_t);
      void (*myPrevFree) (void *, std::size_t);
    };
  private:
    class ZZQQMgr
    {
    public:
      ZZQQMgr();
      ~ZZQQMgr(); // prints rude message if ZZ or QQ still have external references
    public: // data members of ZZQQMgr
      ring myRingZZ;                 // Must come before RingQQ
      FractionField myRingQQ;
    };
  private: // data members of GlobalManager
    const GlobalSettings::ResidueSetting myResidueSetting; ///< default value used for creating rings Z/(nZ)
    GMPMemMgr myGMPMemMgr;        // Must come before myZZQQMgr
    ZZQQMgr myZZQQMgr;            // Must come IMMEDIATELY after myGMPMemMgr
    mutable RandomSource myRandomSource;  // for global random source (must come after myZZQQMgr)
    struct PtrDtor { void* myPtr; void (*myDtor)(void*); PtrDtor(void* ptr, void (*dtor)(void*)); };
    mutable std::vector<PtrDtor> myDtorList; // really a stack
    friend void RegisterDtorForGlobal(void* ptr, void (*dtor)(void*));

    //-- for Hilbert-Poincare' series --
    static std::size_t ourHPMaxPower; // ??? static?
    mutable std::vector<RingElem> myHPPowerList;
  };


//  bool DefaultResiduesAreSymm();
  GlobalSettings::ResidueSetting DefaultResidueSetting();
  //-- for Hilbert-Poincare' series --
  void RegisterDtorForGlobal(void* ptr, void (*dtor)(void*));

  void MakeGlobalHPPowerList(const DenseUPolyRing& HSRing);
  int HPPowerListMaxDeg();
  ConstRefRingElem HPPowerList(int exp);
  void CopyHPPower(RingElem& res, int exp);
  inline std::size_t GlobalGMPSliceSize() { return GlobalManager::GMPSliceSize; }
  inline MemPool* GlobalGMPPoolPtr() { return GlobalManager::GMPPoolPtr; }
  RandomSource& GlobalRandomSource();

} // end of namespace CoCoA



// RCS header/log in the next few lines.
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/GlobalManager.H,v 1.15 2014/07/01 12:40:14 bigatti Exp $
// $Log: GlobalManager.H,v $
// Revision 1.15  2014/07/01 12:40:14  bigatti
// -- added CopyHPPower
//
// Revision 1.14  2013/06/17 08:54:02  abbott
// Added RegisterDtorForGlobal.
//
// Revision 1.13  2012/12/04 20:04:36  abbott
// Includes new unified random header file.
//
// Revision 1.12  2012/02/08 13:37:35  bigatti
// -- changed Z,Q --> ZZ,QQ
//
// Revision 1.11  2011/05/19 13:54:48  abbott
// Replaced DefaultResiduesAreSymm by DefaultResidueSetting.
//
// Revision 1.10  2011/05/03 10:03:32  abbott
// Added GlobalRandomSource.
// Internally added GlobalManager::ptr to allow neater implementations.
//
// Revision 1.9  2010/11/11 17:45:08  abbott
// Moved GMPMemMgr so that it is a nested class inside GlobalManager.
//
// Revision 1.8  2010/11/05 17:39:14  bigatti
// -- fixed name for HPPowerList function declaration
//
// Revision 1.7  2010/10/29 09:36:04  bigatti
// -- added globals for Hilbert-Poincare' series
//
// Revision 1.6  2010/10/27 20:58:45  abbott
// Major reorganization of GlobalManager and GMPAllocator.
//
// Revision 1.5  2010/10/22 14:03:04  abbott
// Major change to GMPAllocator -- it is now set/activated by the GlobalManager.
// This is a Friday afternoon check-in... hope to check in cleaner code in the
// next few days.
//
// Revision 1.4  2010/09/30 14:28:23  abbott
// Replaced auto_ptrs to RingZ and RingQ by direct values; ctor changed accordingly.
//
// Dtor now checks that ref counts in RingZ and RingQ are correct; if not, a rude
// message is printed on cerr (and the program will probably crash after the
// GlobalManager has been destroyed).
//
// Revision 1.3  2009/05/14 09:39:29  abbott
// Added possibility to specify "symmetric" or "non-negative" residues
// in quotients of ZZ.  Affects printing of elements in quotients of ZZ
// (also changed printing of elements in general quotient rings).
// Consequent changes in several tests.
//
// Revision 1.2  2007/10/30 17:14:12  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.2  2007/03/05 21:33:13  cocoa
// Improved/cleaned GlobalManager; added doc too.
//
// Revision 1.1  2007/03/03 14:02:11  bigatti
// -- "foundations" renamed into "GlobalManager"
//
// Revision 1.1  2007/03/02 16:46:28  cocoa
// New foundations object which calls ctors and dtors of global objects.
//

#endif
