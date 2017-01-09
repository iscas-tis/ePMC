#include <assert.h>
#include <vector>
#include "BDDNode.h"

extern "C" {
    __attribute__ ((visibility("default")))
    XBDDManager *cacwrapper_new_manager(int num_variables) {
        assert(num_variables >= 0);
        try {
            return new XBDDManager(num_variables);
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    void cacwrapper_free_manager(XBDDManager *manager) {
        delete manager;
    }

    __attribute__ ((visibility("default")))
    XManager *cacwrapper_get_xmanager(XBDDManager *bddManager) {
        assert(bddManager != NULL);
        return bddManager->manager();
    }

    __attribute__ ((visibility("default")))
    void cacwrapper_set_max_cache_size(XBDDManager *bddManager, int size) {
        assert(bddManager != NULL);
        bddManager->SetMaxCacheSize(size);
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_or(BDD *op1, BDD *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            return new BDD(*op1 + *op2);
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_and(BDD *op1, BDD *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            return new BDD(*op1 * *op2);
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_xor(BDD *op1, BDD *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            return new BDD(*op1 ^ *op2);
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_nor(BDD *op1, BDD *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            return new BDD(*op1 % *op2);
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_nand(BDD *op1, BDD *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            return new BDD(*op1 | *op2);
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_xnor(BDD *op1, BDD *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        try {
            return new BDD(*op1 & *op2);
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_not(BDD *op) {
        assert(op != NULL);
        try {
            return new BDD(!*op);
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_ite(XBDDManager *manager, BDD *op1, BDD *op2, BDD *op3) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        assert(op3 != NULL);
        try {
            return new BDD(manager->Ite(*op1, *op2, *op3));
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    void cacwrapper_free_bdd(BDD *bdd) {
        assert(bdd != NULL);
        delete bdd;
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_new_one(XBDDManager *manager) {
        assert(manager != NULL);
        try {
            return new BDD(manager->BddOne());
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_new_zero(XBDDManager *manager) {
        assert(manager != NULL);
        try {
            return new BDD(manager->BddZero());
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_new_variable(XBDDManager *manager, int variable) {
        assert(manager != NULL);
        assert(variable >= 0);
        try {
            return new BDD(manager->BddVar(variable));
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    bool cacwrapper_equals(BDD *op1, BDD *op2) {
        assert(op1 != NULL);
        assert(op2 != NULL);
        return *op1 == *op2;
    }

    __attribute__ ((visibility("default")))
    bool cacwrapper_is_leaf(BDD *op) {
        assert(op != NULL);
        return op->manager()->One == op->Node()
                || op->manager()->Zero == op->Node();
    }

    __attribute__ ((visibility("default")))
    bool cacwrapper_get_value(BDD *op) {
        assert(op != NULL);
        assert(cacwrapper_is_leaf(op));
        return op->manager()->One == op->Node();
    }

    __attribute__ ((visibility("default")))
    int cacwrapper_node(BDD *op) {
        assert(op != NULL);
        return op->Node();
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_exist(BDD *op, BDD *cube) {
        assert(op != NULL);
        assert(cube != NULL);
        try {
            return new BDD(op->Exist(*cube));
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_universal(BDD *op, BDD *cube) {
        assert(op != NULL);
        assert(cube != NULL);
        try {
            return new BDD(op->Universal(*cube));
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_and_exist(BDD *op1, BDD *op2, BDD *cube) {
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
    int cacwrapper_get_variable(BDD *op) {
        assert(op != NULL);
        try {
            return op->Variable();
        } catch (std::bad_alloc &ba) {
            return -1;
        }
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_permute(BDD *op, vector<int> *permutation) {
        assert(op != NULL);
        assert(permutation != NULL);
        try {
            return new BDD(op->Permute(*permutation));
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    vector<int> *cacwrapper_new_permutation(int *perm, int size, int num_vars) {
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
    void cacwrapper_free_permutation(vector<int> *permutation) {
        assert(permutation != NULL);
        delete permutation;
    }

    __attribute__ ((visibility("default")))
    BDD *cacwrapper_clone(BDD *op) {
        assert(op != NULL);
        try {
            return new BDD(*op);
        } catch (std::bad_alloc &ba) {
            return NULL;
        }
    }

    __attribute__ ((visibility("default")))
    int cacwrapper_walker_variable(XManager *manager, DD walker) {
        assert(manager != NULL);
        try {
            return manager->Variable(walker);
        } catch (std::bad_alloc &ba) {
            return -1;
        }
    }

    __attribute__ ((visibility("default")))
    int cacwrapper_walker_low(XManager *manager, DD walker) {
        assert(manager != NULL);
        try {
            return manager->Else(walker, manager->Variable(walker));
        } catch (std::bad_alloc &ba) {
            return -1;
        }
    }

    __attribute__ ((visibility("default")))
    int cacwrapper_walker_high(XManager *manager, DD walker) {
        assert(manager != NULL);
        try {
            return manager->Then(walker, manager->Variable(walker));
        } catch (std::bad_alloc &ba){
            return -1;
        }
    }
}
