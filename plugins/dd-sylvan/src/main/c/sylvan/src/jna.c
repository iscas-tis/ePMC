#include <jna.h>

long Sylvan_false() { return sylvan_false; }
long Sylvan_true() { return sylvan_true; }
long Sylvan_not(BDD a) { return sylvan_not(a); }
long Sylvan_ite(BDD a, BDD b, BDD c) {
	LACE_ME
	return sylvan_ite(a,b,c);
}
long Sylvan_xor(BDD a, BDD b) {
	LACE_ME
	return sylvan_xor(a,b);
}
long Sylvan_and(BDD a, BDD b) {
	LACE_ME
	return sylvan_and(a,b);
}
long Sylvan_nand(BDD a, BDD b) {
	LACE_ME
	return sylvan_nand(a,b);
}
long Sylvan_or(BDD a, BDD b) {
	LACE_ME
	return sylvan_or(a,b);
}
long Sylvan_nor(BDD a, BDD b) {
	LACE_ME
	return sylvan_nor(a,b);
}
long Sylvan_imp(BDD a, BDD b) {
	LACE_ME
	return sylvan_imp(a,b);
}
long Sylvan_equiv(BDD a, BDD b) {
	LACE_ME
	return sylvan_equiv(a,b);
}
long Sylvan_diff(BDD a, BDD b) {
	LACE_ME
	return sylvan_diff(a,b);
}
long Sylvan_exists(BDD a, BDD vars) {
	LACE_ME
	return sylvan_exists(a, vars);
}
long Sylvan_forall(BDD a, BDD vars) {
	LACE_ME
	return sylvan_forall(a, vars);
}
long Sylvan_and_exists(BDD a, BDD b, BDD vars) {
	LACE_ME
	return sylvan_and_exists(a,b,vars);
}
long Sylvan_map_empty() {
	return sylvan_map_empty();
}
long Sylvan_compose(BDD a, BDDMAP map) {
	LACE_ME
	return sylvan_compose(a, map);
}
