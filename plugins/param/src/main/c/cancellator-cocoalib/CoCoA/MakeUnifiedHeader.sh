#!/bin/bash

# Script to generate automatically the unified header for CoCoALib;
# does nothing if the unified header file is already up-to-date.

# Check we have zero or one arg.
if [ $# -gt 1 ]
then
  echo                                                                  >/dev/stderr
  echo "ERROR: $0 requires 0 or 1 arg."                                 >/dev/stderr
  exit 1
fi
if [ $# = 1 -a "X$1" != "X--check" ]
then
  echo "ERROR: $0 expected arg \"--check\" but found \"$1\""            >/dev/stderr
  exit 2
fi

# Check we're in the CoCoALib/include/CoCoA/ directory.  Give error if not.
CWD=`pwd`
LAST=`basename "$CWD"`
TMP=`dirname "$CWD"`
LASTBUTONE=`basename "$TMP"`
if [ "$LAST" != "CoCoA" -o "$LASTBUTONE" != "include" ]
then
  echo                                                                         >/dev/stderr
  echo "ERROR: $0 should be run only in the directory CoCoALib/include/CoCoA/" >/dev/stderr
  exit 1
fi


# Crude mutex arrangement to avoid problems when this script is called in parallel.
DO_CHECK=no
MUTEX=".mutex"
# function to clean up the mutex file
cleanup()
{
  /bin/rm  -f "$MUTEX"
}
trap cleanup EXIT SIGHUP SIGINT SIGTERM

if [ $# = 1 ]
then
  DO_CHECK=yes
  # Now do crude "mutex" check
  if [ -f "$MUTEX" ]
  then
    while [ -f "$MUTEX" ]; do sleep 1; done
    exit 0
  fi
  ID="$UID@$HOSTNAME-$$"
  echo "$ID" > "$MUTEX"
  sleep 1
  CHECK_ID=`cat "$MUTEX" 2>&1`
  if [ "$CHECK_ID" != "$ID" ]
  then
    exit 0
  fi
fi


UNIFIED_HDR=library.H

# First check whether UNIFIED_HDR is already up-to-date; if so, just exit.
/bin/rm -rf .prev-hdrs
if [ -f "$UNIFIED_HDR" ]
then
  egrep "^#include *\"CoCoA/" "$UNIFIED_HDR" | cut -b 17- | tr -d '"' > .prev-hdrs
else
  # Special case: if $UNIFIED_HDR doesn't exist, disable checking.
  DO_CHECK=no
  touch .prev-hdrs
fi

/bin/ls -d ./*.H | fgrep -v "$UNIFIED_HDR" | cut -b 3- > .curr-hdrs
/usr/bin/cmp -s .prev-hdrs .curr-hdrs
HDRS_CHANGED=$?
if [ $HDRS_CHANGED = 0 -a "$UNIFIED_HDR" -nt ../../configuration/version ]
then
  # Unified header is up-to-date, so exit with code 0.
  /bin/rm .prev-hdrs .curr-hdrs
###  /bin/rm -rf "$MUTEX"
  exit 0
fi

# Put names of header files in a shell variable for later use
COCOALIBHDRS=`/bin/cat .curr-hdrs`

# Note which headers are new/lost for a possible error mesg.
NEW_HDRS=`diff .prev-hdrs .curr-hdrs | egrep "^>" | tr -d ">"`
LOST_HDRS=`diff .prev-hdrs .curr-hdrs | egrep "^<" | tr -d "<"`
/bin/rm .prev-hdrs .curr-hdrs

if [ "$DO_CHECK" = "yes" -a "$UNIFIED_HDR" -nt ../../configuration/version ]
then
  echo                                             >/dev/stderr
  echo "ERROR:  ***HEADERS HAVE CHANGED***"        >/dev/stderr
  echo "ERROR:"                                    >/dev/stderr
  if [ -n "$LOST_HDRS" ]
  then
    echo "ERROR:  ***HEADERS HAVE BEEN LOST***"    >/dev/stderr
    for losthdr in $LOST_HDRS
    do
      echo "ERROR: --> $losthdr"                   >/dev/stderr
    done
    echo "ERROR:"                                  >/dev/stderr
  fi
  if [ -n "$NEW_HDRS" ]
  then
    echo "ERROR:  ***NEW HEADERS*** have appeared" >/dev/stderr
    for newhdr in $NEW_HDRS
    do
      echo "ERROR: --> $newhdr"                    >/dev/stderr
    done
    echo "ERROR:"                                  >/dev/stderr
  fi
  echo "ADVICE: If the current headers are correct then"             >/dev/stderr
  echo "ADVICE: go to the include/CoCoA/ directory and"              >/dev/stderr
  echo "ADVICE: run ./MakeUnifiedHeader.sh manually to accept them." >/dev/stderr
  echo "ADVICE: Alternatively, if they are not correct, please:"     >/dev/stderr
  if [ -n "$LOST_HDRS" ]
  then
    echo "ADVICE: (*) recover the lost CoCoALib headers;"            >/dev/stderr
  fi
  if [ -n "$NEW_HDRS" ]
  then
    echo "ADVICE: (*) remove any headers not belonging to CoCoALib." >/dev/stderr
  fi
###  /bin/rm -f "$MUTEX"
  exit 1
fi


#######################################################
# OK rebuild the unified header file

source ../../configuration/version
VERSION="$VER_MAJ.$VER_MIN$VER_PATCH"
echo "Rebuilding unified header file for CoCoALib (version $VERSION)"

# Put result into $TMP_FILE then "mv" to correct destination;
# this way avoid problems with parallel "make".
TMP_FILE=tmp-$UID@$HOSTNAME-$$
/bin/rm -rf "$TMP_FILE"


# Move existing unified header into a back-up file (with .old suffix).
if [ -f "$UNIFIED_HDR" ]
then
  /bin/rm -rf "$UNIFIED_HDR.old"
  /bin/mv "$UNIFIED_HDR" "$UNIFIED_HDR.old"
  echo "** Moved existing unified header file into $UNIFIED_HDR.old **"
  echo
fi

# Create preamble at start of unified header file.
# WARNING: the Makefile in the top CoCoALib directory looks for the version number using
#          a string search -- be careful if you change the format produced here!
TODAY=`date "+%Y%m%d+%T"`
echo "#ifndef CoCoA_library_H"                                        >> "$TMP_FILE"
echo "#define CoCoA_library_H"                                        >> "$TMP_FILE"
echo                                                                  >> "$TMP_FILE"
echo "// Unified header file for CoCoALib version $VERSION"           >> "$TMP_FILE"
echo "// (produced automatically by MakeUnifiedHeader.sh on $TODAY)"  >> "$TMP_FILE"
echo                                                                  >> "$TMP_FILE"

# Now do an include directive for each individual header file (in COCOALIBHDRS).
for file in $COCOALIBHDRS
do
  echo "#include \"CoCoA/$file\""                                     >> "$TMP_FILE"
done

# Postamble: close the read-once ifndef directive
echo                                                                  >> "$TMP_FILE"
echo "#endif"                                                         >> "$TMP_FILE"

/bin/mv "$TMP_FILE"  "$UNIFIED_HDR"
# /bin/rm -f "$TMP_FILE"  # in case the /bin/mv command fails???

# Note: MUTEX file is cleaned up by the trap command
