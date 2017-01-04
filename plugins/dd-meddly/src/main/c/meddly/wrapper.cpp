#include <assert.h>
#include <vector>
#include "meddly.h"
#include "meddly_expert.h"

using namespace MEDDLY;

static bool initialized = false;

// TODO At the time of writing this wrapper, Meddly seems quite incomplete. Many
// TODO functions are missing, operator overloads are missing, etc.

extern "C" {
    __attribute__ ((visibility("default")))
    settings *meddlywrapper_new_settings() {
        try {
            return new settings();
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    void meddlywrapper_initialize(const settings *s) {
        assert(s != NULL);
        assert(!initialized);
        initialize(*s);
        initialized = true;
    }

    __attribute__ ((visibility("default")))
    void meddlywrapper_cleanup() {
        assert(initialized);
        cleanup();
        initialized = false;
    }

    __attribute__ ((visibility("default")))
    domain *meddlywrapper_new_domain(int num_variables) {
        assert(num_variables >= 0);
        try {
            int *vars = new int[num_variables];
            for (int varNr = 0; varNr < num_variables; varNr++) {
                vars[varNr] = 2;
            }
            domain *d = createDomainBottomUp(vars, num_variables);
            delete vars;
            return d;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    forest *meddlywrapper_new_forest(domain *d) {
        assert(d != NULL);
        try {
            // TODO forest policies??
            forest::policies::policies p(false);
            p.setFullyReduced();
            forest *f = d->createForest(false, forest::BOOLEAN, forest::MULTI_TERMINAL, p, 0);
            return f;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    void meddlywrapper_free_domain(domain *d) {
        assert(d != NULL);
        destroyDomain(d);
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_or(dd_edge *op1, dd_edge *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            dd_edge *result = new dd_edge();
            *result = *op1 + *op2;
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_and(dd_edge *op1, dd_edge *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            dd_edge *result = new dd_edge();
            *result = *op1 * *op2;
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_xor(dd_edge *op1, dd_edge *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            dd_edge *result = new dd_edge();
            dd_edge orE = *op1 + *op2;
            dd_edge andE = *op1 * *op2;
            dd_edge andNotE;
            apply(COMPLEMENT, andE, andNotE);
            *result = orE * andNotE;
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_nor(dd_edge *op1, dd_edge *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            dd_edge orE = *op1 + *op2;
            dd_edge *result = new dd_edge();
            apply(COMPLEMENT, orE, *result);
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_nand(dd_edge *op1, dd_edge *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            dd_edge andE = *op1 * *op2;
            dd_edge *result = new dd_edge();
            apply(COMPLEMENT, andE, *result);
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_xnor(dd_edge *op1, dd_edge *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            dd_edge eqE;
            apply(EQUAL, *op1, *op2, eqE);
            dd_edge *result = new dd_edge();
            apply(COMPLEMENT, eqE, *result);
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_not(dd_edge *op) {
        assert(op != NULL);
        try {
            dd_edge *result = new dd_edge();
            apply(COMPLEMENT, *op, *result);
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_ite(dd_edge *op1, dd_edge *op2, dd_edge *op3) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        assert(op3 != NULL);
        try {
            dd_edge *result = new dd_edge();
            dd_edge notOp1;
            apply(COMPLEMENT, *op1, notOp1);
            dd_edge op1AndOp2 = *op1 * *op2;
            dd_edge notOp1AndOp3 = notOp1 * *op3;
            *result = op1AndOp2 + notOp1AndOp3;
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    void meddlywrapper_free_bdd(dd_edge *bdd) {
        assert(bdd != NULL);
        delete bdd;
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_new_one(forest *manager) {
        assert(manager != NULL);
        try {
            dd_edge *result = new dd_edge();
            manager->createEdge(true, *result);
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_new_zero(forest *manager) {
        assert(manager != NULL);
        try {
            dd_edge *result = new dd_edge();
            manager->createEdge(false, *result);
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_new_variable(forest *manager, int variable) {
        assert(manager != NULL);
        assert(variable >= 0);
        try {
            dd_edge *result = new dd_edge();
            manager->createEdgeForVar(variable, false, *result);
            return result;
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    bool meddlywrapper_equals(dd_edge *op1, dd_edge *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        return *op1 == *op2;
    }

    __attribute__ ((visibility("default")))
    bool meddlywrapper_is_leaf(dd_edge *op) {
        assert(op != NULL);
        node_handle handle = op->getNode();
        expert_forest *forest = (expert_forest *) op->getForest();
        return forest->isTerminalNode(handle);
    }

    __attribute__ ((visibility("default")))
    bool meddlywrapper_get_value(dd_edge *op) {
        assert(op != NULL);
        assert(meddlywrapper_is_leaf(op));
        node_handle handle = op->getNode();
        expert_forest *forest = (expert_forest *) op->getForest();
        return forest->getBooleanFromHandle(handle);
    }

    __attribute__ ((visibility("default")))
    int meddlywrapper_node(dd_edge *op) {
        assert(op != NULL);
        node_handle handle = op->getNode();
        return handle;
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_exist(dd_edge *op, dd_edge *cube) {
        assert(op != NULL);
        assert(cube != NULL);
        try {
            return new BDD(op->Exist(*cube));
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_universal(dd_edge *op, dd_edge *cube) {
        assert(op != NULL);
        assert(cube != NULL);
        try {
            return new BDD(op->Universal(*cube));
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_and_exist(dd_edge *op1, dd_edge *op2, dd_edge *cube) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        assert(cube != NULL);
        try {
            return new BDD(op1->AndExist(*op2, *cube));
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    int meddlywrapper_get_variable(dd_edge *op) {
        assert(op != NULL);
        try {
            return op->Variable();
        } catch (std::bad_alloc &ba) {
            return -1;
        }
    }

    __attribute__ ((visibility("default")))
    dd_edge *meddlywrapper_permute(dd_edge *op, vector<int> *permutation) {
        assert(op != NULL);
        assert(permutation != NULL);
        try {
            return new BDD(op->Permute(*permutation));
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    vector<int> *meddlywrapper_new_permutation(int *perm, int size, int num_vars) {
        assert(perm != NULL);
        assert(size >= 0);
        assert(num_vars >= 0);
        vector<int> * result;
        try {
            result = new vector<int>();
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
        try {
            result->push_back(0);
            for (int i = 0; i < size; i++) {
                result->push_back(perm[i] + 1);
            }
            for (int i = size; i < num_vars + 1; i++) {
                result->push_back(i + 1);
            }
        } catch (std::bad_alloc &ba) {
            delete result;
            return NULL;
        }
        return result;
    }

    __attribute__ ((visibility("default")))
    void meddlywrapper_free_permutation(vector<int> *permutation) {
        assert(permutation != NULL);
        delete permutation;
    }

    __attribute__ ((visibility("default")))
    BDD *meddlywrapper_clone(BDD *op) {
        assert(op != NULL);
        try {
            return new BDD(*op);
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    int meddlywrapper_walker_variable(XManager *manager, DD walker) {
        assert(manager != NULL);
        try {
            return manager->Variable(walker);
        } catch (std::bad_alloc &ba) {
            return -1;
        }
    }

    __attribute__ ((visibility("default")))
    int meddlywrapper_walker_low(XManager *manager, DD walker) {
        assert(manager != NULL);
        try {
            return manager->Else(walker, manager->Variable(walker));
        } catch (std::bad_alloc &ba) {
            return -1;
        }
    }

    __attribute__ ((visibility("default")))
    int meddlywrapper_walker_high(XManager *manager, DD walker) {
        assert(manager != NULL);
        try {
            return manager->Then(walker, manager->Variable(walker));
        } catch (std::bad_alloc &ba){
            return -1;
        }
    }
}
