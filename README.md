# EPMC - (An Extendible Probabilistic Model Checker, previously known as IscasMC)

EPMC is a successor of the model checker IscasMC which only focuses on PLTL model checking over MDP.

Compared to IscasMC, EPMC supports a richer set of probabilistic models including

* Discrete Time Markov Chains (DTMCs)
* Markov Decision Processes (MDPs)
* Continuous Time Markov Chains (CTMCs)
* Stochastic Multi-player games (SMGs)
* Probabilistic Parity Games (PPGs)

All input models can be specified in the PRISM format and JANI format.

EPMC implemented the supports for

* PCTL
* PLTL
* PCTL*
* CSL (in progress)
* Transient Properties (in progress)
* Expected Rewards (in progress)

The main characteristics of EPMC are *the high modularity of the tool, the possibility to extend EPMC with plugins to add new functionalities, and its availability on multiple platforms*. EPMC achieves its flexibility by an infrastructure that consists of a minimal core part and multiple plugins that is very convenient to develop a new model checker based on the core parts of EPMC.

EPMC is mainly developed in Java, but accesses a few libraries written in C/C++ to increase performance or to access well established legacy code. Its graphical user interface (GUI) is a single static webpage. The GUI communicates with the backend, where core functions (like model checking) and high-privilege operations (like file I/O) are realised.

Please follow our [wiki documentations](../../wiki/Documentations) to build and use ePMC.

## Contact
Comments and feedback about any aspect of ePMC are very welcome. Please contact:

Andrea Turrini

(family_name_AT_ios_DOT_ac_DOT_cn)
