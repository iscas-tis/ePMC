# written by LI. Yi
# created on Feb 23rd, 2017
ROOT="$( cd "$( dirname "$0"  )" && pwd  )"
TESTDIR="tests/src/test/resources/epmc/"
: ${EPMC_MEMSIZE:="8G"}

if [ "$1" != "" ]; then
    java -ea -Xms${EPMC_MEMSIZE} -Xmx${EPMC_MEMSIZE} -jar epmc-standard.jar explore --model-input-files $ROOT/$TESTDIR/$1.prism `if [ -e $ROOT/$TESTDIR/$1.clo ]; then cat $ROOT/$TESTDIR/$1.clo; fi`
    java -ea -Xms${EPMC_MEMSIZE} -Xmx${EPMC_MEMSIZE} -jar epmc-standard.jar check --model-input-files $ROOT/$TESTDIR/$1.prism --property-input-files $ROOT/$TESTDIR/$1.pctl `if [ -e $ROOT/$TESTDIR/$1.clo ]; then cat $ROOT/$TESTDIR/$1.clo; fi`
else
    
    echo 'exploring model:'
    for name in `ls $ROOT/$TESTDIR | grep .prism`
    do
        echo "- $name: "
		filename=`basename "$name" .prism`
        java -Xms${EPMC_MEMSIZE} -Xmx${EPMC_MEMSIZE} -jar epmc-standard.jar explore --model-input-files $ROOT/$TESTDIR/$name `if [ -e $ROOT/$TESTDIR/${filename}.clo ]; then cat $ROOT/$TESTDIR/${filename}.clo; fi`
    done

fi
