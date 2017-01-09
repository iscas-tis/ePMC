#include <sylvan.h>
#include <sylvan_common.h>
#include <sylvan_mtbdd.h>

typedef MTBDD (*DD_VOP1)(int op, MTBDD);
typedef MTBDD (*DD_VOP2)(int op, MTBDD, MTBDD);
typedef MTBDD (*DD_VOP3)(int op, MTBDD, MTBDD, MTBDD);
typedef int (*GET_OPERATOR_NUMBER)(char *);
typedef MTBDD (*generic_callback)(void);

typedef struct {
	int id;
	int not;
	int add;
	int multiply;
	int subtract;
	int divide;
	int divide_ignore_zero;
	int max;
	int min;
	int and;
	int or;
	int iff;
	int implies;
	int eq;
	int ne;
} Operators;

TASK_DECL_3(MTBDD, mtbdd_custom_ite, MTBDD, MTBDD, MTBDD);
TASK_DECL_3(MTBDD, mtbdd_custom_abstract, MTBDD, MTBDD, mtbdd_apply_op);

TASK_DECL_2(MTBDD, callback_id, MTBDD, size_t);
TASK_DECL_2(MTBDD, callback_not, MTBDD, size_t);
TASK_DECL_2(MTBDD, callback_add, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_multiply, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_subtract, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_divide, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_divide_ignore_zero, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_max, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_min, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_and, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_or, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_iff, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_implies, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_eq, MTBDD*, MTBDD*);
TASK_DECL_2(MTBDD, callback_ne, MTBDD*, MTBDD*);
