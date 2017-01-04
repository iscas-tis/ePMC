#include <sylvan.h>

long Sylvan_false();
long Sylvan_true();
long Sylvan_not(BDD a);
long Sylvan_ite(BDD a, BDD b, BDD c);
long Sylvan_xor(BDD a, BDD b);
long Sylvan_and(BDD a, BDD b);
long Sylvan_nand(BDD a, BDD b);
long Sylvan_or(BDD a, BDD b);
long Sylvan_nor(BDD a, BDD b);
long Sylvan_imp(BDD a, BDD b);
long Sylvan_equiv(BDD a, BDD b);
long Sylvan_diff(BDD a, BDD b);
long Sylvan_exists(BDD a, BDD vars);
long Sylvan_forall(BDD a, BDD vars);
long Sylvan_and_exists(BDD a, BDD b, BDD vars);
long Sylvan_map_empty();
