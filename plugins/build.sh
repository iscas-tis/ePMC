#!/bin/bash

excludes="cuda propertysolver-ltl-fairness propertysolver-ltl-fg"

rm -fr $(find . -name target)

function build_plugin {
    cd $1
    if [ $? -ne 0 ]
    then
	echo 'Plugin directory ' $1 ' does not exist. Exiting.'
	exit
    fi
    mvn clean package
    if [ $? -ne 0 ]
    then
	echo 'Plugin ' $1 ' failed to build. Exiting.'
	exit
    fi
    cd ..
}

for directory in $(ls); do
    directory=$(echo -e $directory| tr -d '\r\n' )
    excluded=0
    for exclude in $excludes; do
	if [ "$exclude" == "$directory" ] 
	then
	    excluded=1
	    break
	fi
    done
    if [ "$excluded" == "1" ]; then
	continue
    fi
    if [ ! -d $directory ]; then
	continue
    fi
    if [ ! -e "$directory/src/main/resources/META-INF/MANIFEST.MF" ]; then
	continue
    fi
    if [ ! -e "$directory/pom.xml" ]; then
	continue
    fi
    manifest=$(cat "$directory/src/main/resources/META-INF/MANIFEST.MF")
    pluginName=$(echo "$manifest" | grep "Plugin-Name" | sed 's/Plugin-Name: //')
    pluginName=$(echo $pluginName | tr -d '\r\n' )
    if [ "$pluginName" = "" ]; then
	continue
    fi
    if [ "$pluginName" != "$directory" ]; then
	continue
    fi
    pluginDependencies=$(echo "$manifest" | grep "Plugin-Dependencies" | sed 's/Plugin-Dependencies: //' | tr -d '\r\n' )
    for pluginDependency in $pluginDependencies; do
	toSort="$toSort\n$pluginName $pluginDependency"
    done
    if [ "$pluginDependencies" = "" ]; then
	toSort="$toSort\n$pluginName $pluginName"
    fi
done

ordered=$(echo -e $toSort | tsort)

for a in $ordered; do
    reordered="$a $reordered"
done

for a in $reordered; do
    build_plugin $a
done
