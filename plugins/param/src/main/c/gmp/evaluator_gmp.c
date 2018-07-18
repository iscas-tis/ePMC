#include <stdlib.h>
#include <assert.h>
#include "gmp.h"

const int OP_PARAMETER = 0;
const int OP_NUMBER = 1;
const int OP_ADD_INVERSE = 2;
const int OP_MULTIPLY_INVERSE = 3;
const int OP_ADD = 4;
const int OP_MULTIPLY = 5;

static int get_operator(int *program, int statement) {
    return program[statement * 4];
}

static int get_assigned_to(int *program, int statement) {
    return program[statement * 4 + 1];
}

static int get_operand_left(int *program, int statement) {
    return program[statement * 4 + 2];
}

static int get_operand_right(int *program, int statement) {
    return program[statement * 4 + 3];
}

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
