#ifndef EPMC_polynomial_sort_H
#define EPMC_polynomial_sort_H

// namespace epmc // TODO
 {
  using namespace std;

  struct TermRef;

  struct Term {
    Term &operator=(const Term &from) {
      value = from.value;
      col = from.col;
      lower = from.lower;
      upper = from.upper;
      return *this;
    }

    operator TermRef();

	int numParameters;
	vector<int> monomials;
	int *monomials;
	char *coefficient;
  };

  struct TermRef {
    TermRef(double *value__, unsigned *col__,
		     double *lower__, double *upper__) :
      value(*value__), col(*col__), lower(*lower__), upper(*upper__) {
    }

    TermRef operator=(const Term from) {
      value = from.value;
      col = from.col;
      lower = from.lower;
      upper = from.upper;
      return *this;
    }

    TermRef operator=(const TermRef from) {
      value = from.value;
      col = from.col;
      lower = from.lower;
      upper = from.upper;
      return *this;
    }

    operator Term() {
      Term result;
      result.value = value;
      result.col = col;
      result.lower = lower;
      result.upper = upper;
      return result;
    }

    operator const Term() const {
      Term result;
      result.value = value;
      result.col = col;
      result.lower = lower;
      result.upper = upper;
      return result;
    }

    double &value;
    unsigned &col;
    double &lower;
    double &upper;
  };

  Term::operator TermRef() {
    TermRef result(&value, &col, &lower, &upper);
    return result;
  }


  struct TargetCmpLower {
    bool operator()(const TermRef e1, const TermRef e2) {
      return e1.value < e2.value;
    }
  } targetCmpLower;

  struct TargetCmpHigher {
    bool operator()(const TermRef e1, const TermRef e2) {
      return e1.value > e2.value;
    }
  } targetCmpHigher;


  struct TargetIterator {
    typedef random_access_iterator_tag iterator_category;
    typedef Term value_type;
    typedef ptrdiff_t difference_type;
    typedef Term* pointer;
    typedef TermRef reference;

    TermRef operator*() const {
      TermRef result(value, col, lower, upper);
      return result;
    }
    
    TargetIterator &operator++() {
      value++;
      col++;
      lower++;
      upper++;
      return *this;
    }

    TargetIterator &operator--() {
      value--;
      col--;
      lower--;
      upper--;
      return *this;
    }

    TargetIterator &operator+=(difference_type inc) {
      value+=inc;
      col+=inc;
      lower+=inc;
      upper+=inc;
      return *this;
    }

    double *value;
    unsigned *col;
    double *lower;
    double *upper;
  };
  
  TargetIterator operator+(const TargetIterator& it, size_t val) {
    TargetIterator result = it;
    result.value += val;
    result.col += val;
    result.lower += val;
    result.upper += val;
    return result;
  }

  TargetIterator operator-(const TargetIterator& it, size_t val) {
    TargetIterator result = it;
    result.value -= val;
    result.col -= val;
    result.lower -= val;
    result.upper -= val;
    return result;
  }

  bool operator<(const TargetIterator &it1, const TargetIterator &it2) {
    return it1.value < it2.value;
  }

  bool operator>(const TargetIterator &it1, const TargetIterator &it2) {
    return it1.value > it2.value;
  }

  bool operator<=(const TargetIterator &it1, const TargetIterator &it2) {
    return it1.value <= it2.value;
  }

  bool operator>=(const TargetIterator &it1, const TargetIterator &it2) {
    return it1.value >= it2.value;
  }


  bool operator==(const TargetIterator &it1, const TargetIterator &it2) {
    return (it1.value == it2.value);
  }

  bool operator!=(const TargetIterator &it1, const TargetIterator &it2) {
    return it1.value != it2.value;
  }

  ptrdiff_t operator-(const TargetIterator &it1, const TargetIterator &it2) {
    return it1.value - it2.value;
  }

  void swap(TermRef it1, TermRef it2) {
    Term temp = it1;
    it1 = it2;
    it2 = temp;
  }
}

#endif
