.PHONY:	lr ld lp lsh config all
all:	lr lsh
static:	lr

## Load Previous Configuration ####################################################################

-include config.mk

## Configurable options ###########################################################################

# Directory to store object files, libraries, executables, and dependencies:
BUILD_DIR  ?= build

# Include debug-symbols in release builds
MBINDC_RELSYM ?= -g

# Sets of compile flags for different build types
MBINDC_REL    ?= -O3 -D NDEBUG
MBINDC_DEB    ?= -O0 -D DEBUG 
MBINDC_PRF    ?= -O3 -D NDEBUG
MBINDC_FPIC   ?= -fpic

# Dependencies
MINISAT_INCLUDE?=
MINISAT_LIB    ?=-lminisat

# GNU Standard Install Prefix
prefix         ?= /usr/local

## Write Configuration  ###########################################################################

config:
	@( echo 'BUILD_DIR?=$(BUILD_DIR)'            ; \
	   echo 'MBINDC_RELSYM?=$(MBINDC_RELSYM)'	     ; \
	   echo 'MBINDC_REL?=$(MBINDC_REL)'      	     ; \
	   echo 'MBINDC_DEB?=$(MBINDC_DEB)'      	     ; \
	   echo 'MBINDC_PRF?=$(MBINDC_PRF)'      	     ; \
	   echo 'MBINDC_FPIC?=$(MBINDC_FPIC)'    	     ; \
	   echo 'MINISAT_INCLUDE?=$(MINISAT_INCLUDE)'; \
	   echo 'MINISAT_LIB?=$(MINISAT_LIB)'	     ; \
	   echo 'prefix?=$(prefix)'                  ) > config.mk

## Configurable options end #######################################################################

INSTALL ?= install

# GNU Standard Install Variables
exec_prefix ?= $(prefix)
includedir  ?= $(prefix)/include
bindir      ?= $(exec_prefix)/bin
libdir      ?= $(exec_prefix)/lib
datarootdir ?= $(prefix)/share
mandir      ?= $(datarootdir)/man

# Target file names
MBINDC_SLIB = libminisat-c.a#  Name of MiniSat C-bindings static library.
MBINDC_DLIB = libminisat-c.so# Name of MiniSat C-bindings shared library.

# Shared Library Version
SOMAJOR=1
SOMINOR=0
SORELEASE=.0

MBINDC_CXXFLAGS = -I. -D __STDC_LIMIT_MACROS -D __STDC_FORMAT_MACROS -Wall -Wno-parentheses -Wextra $(MINISAT_INCLUDE)
MBINDC_LDFLAGS  = -Wall -lz $(MINISAT_LIB)

ifeq ($(VERB),)
ECHO=@
VERB=@
else
ECHO=#
VERB=
endif

SRCS = $(wildcard *.cc)
HDRS = $(wildcard *.h)
OBJS = $(SRCS:.cc=.o)

lr:	$(BUILD_DIR)/release/lib/$(MBINDC_SLIB)
ld:	$(BUILD_DIR)/debug/lib/$(MBINDC_SLIB)
lp:	$(BUILD_DIR)/profile/lib/$(MBINDC_SLIB)
lsh:	$(BUILD_DIR)/dynamic/lib/$(MBINDC_DLIB).$(SOMAJOR).$(SOMINOR)$(SORELEASE)

## Build-type Compile-flags:
$(BUILD_DIR)/release/%.o:			MBINDC_CXXFLAGS +=$(MBINDC_REL) $(MBINDC_RELSYM)
$(BUILD_DIR)/debug/%.o:				MBINDC_CXXFLAGS +=$(MBINDC_DEB) -g
$(BUILD_DIR)/profile/%.o:			MBINDC_CXXFLAGS +=$(MBINDC_PRF) -pg
$(BUILD_DIR)/dynamic/%.o:			MBINDC_CXXFLAGS +=$(MBINDC_REL) $(MBINDC_FPIC)

## Library dependencies
$(BUILD_DIR)/release/lib/$(MBINDC_SLIB):	$(foreach o,$(OBJS),$(BUILD_DIR)/release/$(o))
$(BUILD_DIR)/debug/lib/$(MBINDC_SLIB):		$(foreach o,$(OBJS),$(BUILD_DIR)/debug/$(o))
$(BUILD_DIR)/profile/lib/$(MBINDC_SLIB):	$(foreach o,$(OBJS),$(BUILD_DIR)/profile/$(o))
$(BUILD_DIR)/dynamic/lib/$(MBINDC_DLIB).$(SOMAJOR).$(SOMINOR)$(SORELEASE):	$(foreach o,$(OBJS),$(BUILD_DIR)/dynamic/$(o))

## Compile rules (these should be unified, buit I have not yet found a way which works in GNU Make)
$(BUILD_DIR)/release/%.o:	%.cc
	$(ECHO) echo Compiling: $@
	$(VERB) mkdir -p $(dir $@) $(dir $(BUILD_DIR)/dep/$*.d)
	$(VERB) $(CXX) $(MBINDC_CXXFLAGS) $(CXXFLAGS) -c -o $@ $< -MMD -MF $(BUILD_DIR)/dep/$*.d

$(BUILD_DIR)/profile/%.o:	%.cc
	$(ECHO) echo Compiling: $@
	$(VERB) mkdir -p $(dir $@) $(dir $(BUILD_DIR)/dep/$*.d)
	$(VERB) $(CXX) $(MBINDC_CXXFLAGS) $(CXXFLAGS) -c -o $@ $< -MMD -MF $(BUILD_DIR)/dep/$*.d

$(BUILD_DIR)/debug/%.o:	%.cc
	$(ECHO) echo Compiling: $@
	$(VERB) mkdir -p $(dir $@) $(dir $(BUILD_DIR)/dep/$*.d)
	$(VERB) $(CXX) $(MBINDC_CXXFLAGS) $(CXXFLAGS) -c -o $@ $< -MMD -MF $(BUILD_DIR)/dep/$*.d

$(BUILD_DIR)/dynamic/%.o:	%.cc
	$(ECHO) echo Compiling: $@
	$(VERB) mkdir -p $(dir $@) $(dir $(BUILD_DIR)/dep/$*.d)
	$(VERB) $(CXX) $(MBINDC_CXXFLAGS) $(CXXFLAGS) -c -o $@ $< -MMD -MF $(BUILD_DIR)/dep/$*.d

## Static Library rule
%/lib/$(MBINDC_SLIB):
	$(ECHO) echo Linking Static Library: $@
	$(VERB) mkdir -p $(dir $@)
	$(VERB) $(AR) -rcs $@ $^

## Shared Library rule
$(BUILD_DIR)/dynamic/lib/$(MBINDC_DLIB).$(SOMAJOR).$(SOMINOR)$(SORELEASE):
	$(ECHO) echo Linking Shared Library: $@
	$(VERB) mkdir -p $(dir $@)
	$(VERB) $(CXX) -o $@ -shared -Wl,-soname,$(MBINDC_DLIB).$(SOMAJOR) $^ $(MBINDC_LDFLAGS)

install:	install-headers install-lib install-lib-static
install-static:	install-headers install-lib-static

install-headers:
#       Create directories
	$(INSTALL) -d $(DESTDIR)$(includedir)/mcl
#       Install headers
	for h in $(HDRS) ; do \
	  $(INSTALL) -m 644 $$h $(DESTDIR)$(includedir)/$$h ; \
	done

install-lib: $(BUILD_DIR)/dynamic/lib/$(MBINDC_DLIB).$(SOMAJOR).$(SOMINOR)$(SORELEASE)
	$(INSTALL) -d $(DESTDIR)$(libdir)
	$(INSTALL) -m 644 $(BUILD_DIR)/dynamic/lib/$(MBINDC_DLIB).$(SOMAJOR).$(SOMINOR)$(SORELEASE) $(DESTDIR)$(libdir)
	ln -sf $(MBINDC_DLIB).$(SOMAJOR).$(SOMINOR)$(SORELEASE) $(DESTDIR)$(libdir)/$(MBINDC_DLIB).$(SOMAJOR)
	ln -sf $(MBINDC_DLIB).$(SOMAJOR) $(DESTDIR)$(libdir)/$(MBINDC_DLIB)

install-lib-static: $(BUILD_DIR)/release/lib/$(MBINDC_SLIB)
	$(INSTALL) -d $(DESTDIR)$(libdir)
	$(INSTALL) -m 644 $(BUILD_DIR)/release/lib/$(MBINDC_SLIB) $(DESTDIR)$(libdir)

## Include generated dependencies
## NOTE: dependencies are assumed to be the same in all build modes at the moment!
-include $(foreach s, $(SRCS:.cc=.d), $(BUILD_DIR)/dep/$s)
