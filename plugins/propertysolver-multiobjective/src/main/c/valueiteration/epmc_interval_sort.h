#ifndef EPMC_INTERVAL_SORT_H
#define EPMC_INTERVAL_SORT_H

static inline void interval_insertion_sort(double *values, int *cols,
        double *lowers, double *uppers, int left, int right) {
    for (int i = left + 1; i < right; i++ ) {
        double values_save = values[i];
        int cols_save = cols[i];
        double lowers_save = lowers[i];
        double uppers = uppers[i];
        int j;
        for (j = i; j > 0 && values[j - 1] > values_save; j--) {
            values[j] = values[j - 1];
        }

        values[j] = values_save;
        cols[j] = cols_save;
        lowers[j] = lowers_save;
        uppers[j] = uppers_save;
    }
}

double inverval_median(int *values, int *cols, double *lowers, double *uppers,
        int left, int right) {
  int mid = (left + right) / 2;

  if (values[left] > values[mid]) {
    swap(&values[left], &cols[left], &lowers[left], &uppers[left],
         &values[mid], &cols[mid], &lowers[mid], &uppers[mid]);
  }

  if (values[left] > values[right]) {
      swap(&values[left], &cols[left], &lowers[left], &uppers[left],
           &values[right], &cols[right], &lowers[right], &uppers[right]);
  }

  if (values[mid] > values[right]) {
      swap(&values[mid], &cols[mid], &lowers[mid], &uppers[mid],
           &values[right], &cols[right], &lowers[right], &uppers[right]);
  }

  swap(&values[mid], &cols[mid], &lowers[mid], &uppers[mid],
       &values[right - 1], &cols[right - 1], &lowers[right - 1], &uppers[right - 1]);

  return values[right - 1];
}

struct pivots partition(int *values, int *cols, double *lowers, double *uppers,
        int left, int right) {
  int k;
  int i = left;
  int j = right - 1;
  int m = left;
  int n = right - 1;
  double pivot = interval_median(values, cols, lowers, uppers, left, right);
  struct pivots ret;

  /* Three way partition <,==,> */
  for ( ; ; ) {
    while (values[++i] < pivot) {
    }
    while (values[--j] > pivot) {
      if (j == left)
        break;
    }

    if (i >= j) {
      break;
    }

    swap(&values[i], &cols[i], &lowers[i], &uppers[i],
         &values[j], &cols[j], &lowers[j], &uppers[j]);

    if (values[i] == pivot) {
      ++m;
      swap(&values[m], &cols[m], &lowers[m], &uppers[m],
           &values[i], &cols[i], &lowers[i], &uppers[i]);
    }

    if (values[j] == pivot) {
      --n;
      swap(&values[n], &cols[n], &lowers[n], &uppers[n]
           &values[j], &cols[j], &lowers[j], &uppers[j]);
    }
  }

  swap(&values[i], &cols[i], &lowers[i], &uppers[i],
       &values[right - 1], &cols[right - 1], &lowers[right - 1], &uppers[right - 1]);

  j = i - 1;
  i = i + 1;

  for (k = left; k < m; k++, j--) {
    swap(&values[k],
         &values[j]);
  }

  for (k = right - 1; k > n; k--, i++) {
    swap(&values[k],
         &values[i]);
  }

  ret.left = i;
  ret.right = j;

  return ret;
}

#if 0
void quicksort_r(int list[], int left, int right) {
  /* Terminate on small subfiles */
  if (left + CUTOFF <= right) {
    struct pivots pivot = partition ( list, left, right );

    quicksort_r ( list, left, pivot.right );
    quicksort_r ( list, pivot.left, right );
  }
}

void quicksort(int list[], int left, int right) {
  quicksort_r(list, left, right - 1);

  /* Insertion sort on almost sorted list */
  insertion_sort (list, left, right);
}
#endif

#endif
