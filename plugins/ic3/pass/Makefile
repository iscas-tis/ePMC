###############################################
# global Makefile with automatic dependencies #
###############################################

HOME_BOOST_43 := $(shell test -d $(HOME)/boost_1_43_0 && echo yes)
HOME_BOOST_42 := $(shell test -d $(HOME)/boost_1_42_0 && echo yes)
HOME_BOOST_41 := $(shell test -d $(HOME)/boost_1_41_0 && echo yes)

ifeq ($(HOME_BOOST_43),yes)
	BOOST_PATH = $(HOME)/boost_1_43_0
	BOOST_VERSION = 43
else ifeq ($(HOME_BOOST_41),yes)
	BOOST_PATH = $(HOME)/boost_1_41_0
	BOOST_VERSION = 41
else ifeq ($(HOME_BOOST_42),yes)
	BOOST_PATH = $(HOME)/boost_1_42_0
	BOOST_VERSION = 42
else
	BOOST_PATH =
endif

ifeq ($(BOOST_PATH),)
BOOST_H = /usr/include
else
BOOST_H = $(BOOST_PATH)
endif

ifndef OSTYPE
  OSTYPE = $(shell uname -s|awk '{print tolower($$0)}')-$(shell uname -m)
endif

# check if 64 bit or 32 bit
# => load suitable libraries
LBITS := $(shell getconf LONG_BIT)
ifeq ($(LBITS),64)
   # do 64 bit stuff here, like set some CFLAGS
YICES = -lyices64
MATHSAT = -lmathsat64
else
   # do 32 bit stuff here
YICES = -lyices
MATHSAT = -lmathsat
endif


CVC_PATH = cvc3
CVC_LIB  = $(CVC_PATH)/lib-$(OSTYPE)
CVC_INC  = $(CVC_PATH)/include


MATHSAT_PATH = ./mathsat
MATHSAT_LIB = $(MATHSAT_PATH)/lib
MATHSAT_INC = $(MATHSAT_PATH)/include

ifeq ($(OS),Windows_NT) # CYGWIN
LIB    = -lyices_cygwin -Ldp
else
LIB    = -L/opt/local/lib -lgmp -lgmpxx -Ldp -L$(CVC_LIB) -lcvc3 -L. 
endif


pass: LIB := $(YICES) $(LIB)
pass: LIB := $(YICES) $(LIB)
pass: LIB := $(LIB) -L$(MATHSAT_LIB) $(MATHSAT)

GCC_VERSION := $(shell gcc -dumpversion)
GCC_SHORT_VERSION=43
ifeq ($(BOOST_PATH),)
LIB_BOOST_PO = /usr/lib/libboost_program_options-mt.a
else
LIB_BOOST_PO =  $(BOOST_PATH)/bin.v2/libs/program_options/build/gcc-$(GCC_VERSION)/release/link-static/threading-multi/libboost_program_options.a
endif

##################
# compiler flags #
##################

CUDD_DIR     = bdd/cudd
INCLUDE_DIRS = -I. -I$(CUDD_DIR)/include  -I$(CVC_INC) -I$(BOOST_H)
pass: INCLUDE_DIRS := $(INCLUDE_DIRS) -I$(MATHSAT_INC)

ifeq ($(shell uname -m),x86_64)
CPU = native
else
CPU = i686
endif

# FLAGS        = -O3 -Wall -g -march=$(CPU) -pipe $(INCLUDE_DIRS) -DWno_deprecated # -O2 # -mfpma# th=sse -msse2 -O2
# FLAGS        = -Wall -g -O2 -march=$(CPU) -pipe $(INCLUDE_DIRS)# -O2 # -mfpmath=sse -msse2 -O2
FLAGS        = -ffunction-sections -fdata-sections -Wall -Os -march=$(CPU) -pipe $(INCLUDE_DIRS) 
LDFLAGS =   -Wl,-static -fvtable-gc -Wl,--gc-sections
#-Wl,-s
# FLAGS        = -Wall -O0 -ggdb -march=$(CPU) -pipe $(INCLUDE_DIRS) # -O2 # -mfpmath=sse -msse2 -O2

pass: FLAGS := $(FLAGS)

CXXFLAGS     = $(FLAGS)

#################################
# linking against external code #
#################################

# CUDD_DIR = bdd/cudd-2.4.1
CUDD   := -L$(CUDD_DIR)/obj -L$(CUDD_DIR)/cudd -L$(CUDD_DIR)/mtr -L$(CUDD_DIR)/st -L$(CUDD_DIR)/util -L$(CUDD_DIR)/epd \
          -lcudd -lmtr -lst -lutil -lepd

################
# object files #
################

DIRS     := util lang pred bdd dp model_checker
OBJ_UTIL := util/Util.o          \
            util/Timer.o         \
            util/Statistics.o    \
            util/Database.o      \
            util/aiSee.o
OBJ_LANG := lang/AST.o           \
	    lang/PRISMParser.o   \
	    lang/PRISM.tab.o     \
	    lang/PRISM.yy.o      \
        lang/ExprManager.o   \
	    lang/Model.o         \
	    lang/Property.o
OBJ_DP   := dp/SMT.o         \
	    dp/YicesSMT.o        \
	    dp/CVC3SMT.o         \
	    dp/CLP.o		     \
	    dp/FOCI.o		     \
	    dp/FOCIParser.o      \
	    dp/CSIsat.o          \
	    dp/MathSat.o

OBJ_BDD  := bdd/BDD.o            \
	        bdd/ODD.o
OBJ_PRED := \
	    pred/EncodingManager.o \
	    pred/Predicate.o     \
	    pred/CFG.o           \
	    pred/CFGRefine.o     \
	    pred/CFGaiSee.o      \
	    pred/PredSet.o       \
	    pred/AbsModel.o      \
	    pred/AbsExpression.o \
	    pred/AbsCommand.o    \
	    pred/AbsModelImpl.o  \
	    pred/CEAnalysis.o    \
	    pred/TransitionConstraint.o \
	    pred/Cluster.o  \
	    pred/Decomposition.o \
	    pred/CFGCommand.o

OBJ_MODEL_CHECKER := \
	     model_checker/MDPSparse.o \
         model_checker/ActionGraph.o \
         model_checker/FoxGlynn.o

PASS_OBJ :=  $(OBJ_PRED) $(OBJ_BDD) $(OBJ_DP)

FRONTEND_OBJ := $(OBJ_LANG) $(OBJ_UTIL) $(OBJ_MODEL_CHECKER)

TEST_OBJ := $(OBJ_LANG) $(OBJ_UTIL) $(OBJ_DP) $(OBJ_BDD) model_checker/GameGraphExample.o $(OBJ_MODEL_CHECKER)

OBJ := $(FRONTEND_OBJ) $(PASS_OBJ)

####################
# build everything #
####################
all : 	pass

####################
# fociServer       #
####################
dp/fociServer: dp/fociServer.c
#	gcc -O2 -Wall -lpthread dp/fociServer.c -o dp/fociServer
	gcc -O2 -Wall -pthread dp/fociServer.c -o dp/fociServer

dp/csisatServer: dp/csisatServer.c
#	gcc -O2 -Wall -lpthread dp/csisatServer.c -o dp/csisatServer
	gcc -O2 -Wall -pthread dp/csisatServer.c -o dp/csisatServer
##############
# build CUDD #
##############
$(CUDD_DIR)/cudd/libcudd.a:
	cd bdd/cudd; make ; cd ../..
	ranlib $(CUDD_DIR)/cudd/libcudd.a
	ranlib $(CUDD_DIR)/mtr/libmtr.a
	ranlib $(CUDD_DIR)/st/libst.a
	ranlib $(CUDD_DIR)/epd/libepd.a

#####################################
# static library: PASS object files #
#####################################
libfrontend.a : $(FRONTEND_OBJ)
	@ar -rus libfrontend.a $(FRONTEND_OBJ)

libpass.a : $(PASS_OBJ) $(FRONTEND_OBJ)
	@ar -rus libpass.a $(FRONTEND_OBJ)
	@ar -rus libpass.a $(PASS_OBJ)

####################
# build executable #
####################
pass:	libfrontend.a libpass.a main.o dp/fociServer dp/csisatServer $(CUDD_DIR)/cudd/libcudd.a
	$(CXX)   $(FLAGS) -o pass main.o -lpass $(LIB) -lgmpxx -lgmp $(CUDD) $(LDFLAGS) -static
	@echo Build is complete.

###############################
# test for game model checker #
###############################
game:	$(TEST_OBJ) dp/fociServer dp/csiServer
	$(CXX) $(FLAGS) -o game $(TEST_OBJ) $(LIB) $(CUDD)

####################
# build CUDD 2.4.1 #
####################
bdd/cudd-2.4.1/include/util.h :	
	cd bdd/cudd-2.4.1/; make clean; make; cd ../..

###############
# build lexer #
###############
lang/PRISM.yy.cpp :	lang/PRISM.l lang/PRISM.tab.hpp
	cd lang;\
	flex -PPRISM -oPRISM.yy.cpp PRISM.l

################
# build parser #
################
lang/PRISM.tab.hpp lang/PRISM.tab.cpp :	lang/PRISM.ypp
	cd lang;\
	bison -p PRISM -b PRISM -d -t PRISM.ypp

###########
# cleanup #
###########
clean:
	# @if [ -e Makefile.dep ]; then rm Makefile.dep; fi
	@for obj in $(OBJ); do \
		(if [ -e $$obj ]; then rm $$obj; fi) \
		done
	@for dir in $(DIRS); do \
		(cd $$dir; \
		 rm -rf *.o lang/parser.tab.{hpp,cpp} lang/lex.yy.c .*.d *~;\
		 cd ..) \
		done

######################
# build dependencies #
######################
Makefile.dep:
	@if [ ! -e Makefile.dep ]; then echo "# automatic dependencies" > Makefile.dep; fi
	@makedepend -w -a -Y -fMakefile.dep -- $(FLAGS) $(INCLUDE_DIRS) -- main.cpp &> /dev/null
	@for dir in $(DIRS); do \
		(makedepend -w -a -Y -fMakefile.dep -- $(FLAGS) $(INCLUDE_DIRS) -- $$dir/*.hpp $$dir/*.h $$dir/*.cpp) \
	done &> /dev/null

depend:
	@if [ -e Makefile.dep ]; then rm Makefile.dep; fi
	make Makefile.dep

########################################
# automatically generated dependencies #
########################################
include Makefile.dep
