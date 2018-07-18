#ifndef CoCoA_SmartPtrIRCCOW_H
#define CoCoA_SmartPtrIRCCOW_H

//   Copyright (c)  2010  John Abbott, Anna Bigatti

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


#include <cstddef>
//using std::size_t;

namespace CoCoA
{
  // This template class assumes that T derives from the class IntrusiveReferenceCountCOWBase (below).
  template <typename T> class SmartPtrIRCCOW
  {
  public:
    explicit SmartPtrIRCCOW(T* RawPtr): mySmartPtr(RawPtr) {RawPtr->IneedIRCCOWBase();}
    SmartPtrIRCCOW(const SmartPtrIRCCOW& ToBeCopied): mySmartPtr(ToBeCopied.mySmartPtr) {}
    SmartPtrIRCCOW& operator=(const SmartPtrIRCCOW& rhs)
    { // should use assignment:       mySmartPtr = rhs.mySmartPtr;
      SmartPtrIRC<T> NewSmartPtr(rhs.mySmartPtr);
      mySmartPtr.mySwap(NewSmartPtr);
      return *this;
    }
    void mySwap(SmartPtrIRCCOW& other) {mySmartPtr.mySwap(other);}
    const T* operator->() const {return mySmartPtr.operator->();}
    T* operator->() {myDetach(); return mySmartPtr.operator->();}
    
    T* myRawPtr() const { return mySmartPtr.myRawPtr(); }    ///< to avoid too many friend stmts

  private:
    void myDetach()
    {
      if (!(myRawPtr()->IhaveRefCountOne()))
      {
        SmartPtrIRC<T> NewSmartPtr(myRawPtr()->myClone());
        NewSmartPtr.myRawPtr()->myRefCountZero();
        NewSmartPtr.myRawPtr()->myRefCountInc();
        mySmartPtr.mySwap(NewSmartPtr);
      }
    }

  private: // data members
    SmartPtrIRC<T> mySmartPtr;
  };


  // For  ==  and  !=  both args must be pointers to exactly the same type.
  template <typename T>
  inline bool operator==(const SmartPtrIRCCOW<T>& ptr1, const SmartPtrIRCCOW<T>& ptr2)
  {
    return ptr1.myRawPtr() == ptr2.myRawPtr();
  }

  template <typename T>
  inline bool operator!=(const SmartPtrIRCCOW<T>& ptr1, const SmartPtrIRCCOW<T>& ptr2)
  {
    return !(ptr1 == ptr2);
  }


//======================================================================
// IntrusiveReferenceCountCOWBase

// This class is intended to be used solely as a (protected) base class.
  class IntrusiveReferenceCountCOWBase: protected IntrusiveReferenceCount
  {
    template <typename T> friend void SmartPtrIRCCOW<T>::myDetach();
  protected:
    // in addition to IntrusiveReferenceCount members:
    virtual IntrusiveReferenceCountCOWBase* myClone() const =0;
  public:
    void IneedIRCCOWBase() const {}
};
  
  
} // end of namespace CoCoA


// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/TmpSmartPtrIRCCOW.H,v 1.2 2010/07/26 10:41:49 bigatti Exp $
// $Log: TmpSmartPtrIRCCOW.H,v $
// Revision 1.2  2010/07/26 10:41:49  bigatti
// -- removed debugging printouts
//
// Revision 1.1  2010/07/26 09:21:41  bigatti
// -- first import
//
#endif
