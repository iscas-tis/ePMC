#ifndef EPMC_UTIL_H
#define EPMC_UTIL_H

#include <math.h>

static inline double fopt(int min, double x, double y) {
    return min ? fmin(x,y) : fmax(x,y);
}

static inline int is_sorted_descending(double *array, int from, int to) {
    double last_value = INFINITY;
    for (int index = from; index < to; index++) {
        double current_value = array[index];
        if (current_value > last_value) {
            return 0;
        }
        last_value = current_value;
    }
    return 1;
}

static inline void interval_insertion_sort(double *values, int *cols,
        double *lowers, double *uppers, int left, int right);

static inline void swap(
        double *values_a, int *cols_a, double *lowers_a, double *uppers_a,
        double *values_b, int *cols_b, double *lowers_b, double *uppers_b) {
  double values_save = *values_a;
  *values_a = *values_b;
  *values_b = values_save;
  int cols_save = *cols_a;
  *cols_a = *cols_b;
  *cols_b = cols_save;
  double lowers_save = *lowers_a;
  *lowers_a = *lowers_b;
  *lowers_b = lowers_save;
  double uppers_save = *uppers_a;
  *uppers_a = *uppers_b;
  *uppers_b = uppers_save;
}
#endif
