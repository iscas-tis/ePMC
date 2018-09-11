#include <stdlib.h>
#include <assert.h>

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
void evaluate_double(double *variables, int *program, int num_statements, double *numbers, double *point) {
    for (int statement = 0; statement < num_statements; statement++) {
        int operator = get_operator(program, statement);
        double value = variables[get_assigned_to(program, statement)];
        switch (operator) {
        case OP_NUMBER:
            value = numbers[get_operand_left(program, statement)];
            break;
        case OP_PARAMETER:
            value = point[get_operand_left(program, statement) * 2];
            break;
        case OP_ADD_INVERSE:
            value = -variables[get_operand_left(program, statement)];
            break;
        case OP_MULTIPLY_INVERSE:
            value = 1.0/variables[get_operand_left(program, statement)];
            break;
        case OP_ADD:
            value = variables[get_operand_left(program, statement)]
                          + variables[get_operand_right(program, statement)];
            break;
        case OP_MULTIPLY:
            value = variables[get_operand_left(program, statement)]
                          * variables[get_operand_right(program, statement)];
            break;
        default:
            assert(0);
            break;
        }
    }
}
