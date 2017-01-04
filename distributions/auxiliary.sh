#!/bin/bash
TOOLNAME_PATHPART=epmc
function prepare_plugin {
    mkdir $1
    cd $1
    cp ../../../../plugins/${1}/target/${TOOLNAME_PATHPART}-${1}-0-distribution.jar .
    if [ $? -ne 0 ]
    then
	echo 'Plugin ' $1 ' does not exist. Exiting.'
	exit
    fi
    # Note: using "unzip" here, because the directory generated using
    # "jar xf" did not work property for one of the plugins. The switch
    # "-n" is also necessary as otherwise "unzip" seems to overwrite
    # files with name identical except for capitalisation.
    unzip -n ${TOOLNAME_PATHPART}-${1}-0-distribution.jar > /dev/null
    rm ${TOOLNAME_PATHPART}-${1}-0-distribution.jar
    cd ..
    echo $1 >> embeddedplugins.txt
} 

function assemble_begin {
    echo Building EPMC $1 distribution...
    cp ../../main/target/${TOOLNAME_PATHPART}-0-distribution.jar .
    mkdir embeddedplugins 2>/dev/null
    cd embeddedplugins
}

function assemble_end {
    cd ..
    jar uf ${TOOLNAME_PATHPART}-0-distribution.jar embeddedplugins
    rm -fr embeddedplugins
    mv ${TOOLNAME_PATHPART}-0-distribution.jar ${TOOLNAME_PATHPART}-${1}.jar
    echo Done
}

export -f prepare_plugin
export -f assemble_begin
export -f assemble_end
