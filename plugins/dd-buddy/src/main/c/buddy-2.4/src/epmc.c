#include "bdd.h"

void epmc_buddy_silent_gbc_handler(int pre, bddGbcStat *stat) {
    // silence
}

void epmc_buddy_silence() {
    bdd_gbc_hook(epmc_buddy_silent_gbc_handler);
}

int epmc_bdd_relprod(int a, int b, int var) {
    return bdd_relprod(a, b, var);
}
