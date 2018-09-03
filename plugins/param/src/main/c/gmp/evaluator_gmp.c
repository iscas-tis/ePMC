#include <stdlib.h>
#include <assert.h>
#include "gmp.h"

#define OP_PARAMETER 0
#define OP_NUMBER 1
#define OP_ADD_INVERSE 2
#define OP_MULTIPLY_INVERSE 3
#define OP_ADD 4
#define OP_MULTIPLY 5

static inline int get_operator(int *program, int statement) {
    return program[statement * 4];
}

static inline int get_assigned_to(int *program, int statement) {
    return program[statement * 4 + 1];
}

static inline int get_operand_left(int *program, int statement) {
    return program[statement * 4 + 2];
}

static inline int get_operand_right(int *program, int statement) {
    return program[statement * 4 + 3];
}

__attribute__ ((visibility("default")))
void evaluate_gmp(mpq_ptr variables, int *program, int num_statements, mpq_t numbers, mpq_t point) {
    for (int statement = 0; statement < num_statements; statement++) {
        int operator = get_operator(program, statement);
        mpq_ptr value = variables + get_assigned_to(program, statement);
        switch (operator) {
        case OP_NUMBER:
            mpq_set(value, numbers + get_operand_left(program, statement));
            break;
        case OP_PARAMETER:
            mpq_set(value, point + get_operand_left(program, statement) * 2);
            break;
        case OP_ADD_INVERSE:
            mpq_neg(value, variables + get_operand_left(program, statement));
            break;
        case OP_MULTIPLY_INVERSE:
            mpq_inv(value, variables + get_operand_left(program, statement));
            break;
        case OP_ADD:
            mpq_add(value, variables + get_operand_left(program, statement),
                    variables + get_operand_right(program, statement));
            break;
        case OP_MULTIPLY:
            mpq_mul(value, variables + get_operand_left(program, statement),
                    variables + get_operand_right(program, statement));
            break;
        default:
            assert(0);
            break;
        }
    }
}
