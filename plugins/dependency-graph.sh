#!/bin/bash

outputFormat="pdf"
graphvizFilter="dot"
writeFile="0"
onlyBlack="0"
filename="dependency-graph"

function help {
	echo "Usage: $0 [options]"
	echo "Options:"
	echo -e "\t-d\n\t--dot\n\t\tuse \"dot\" as graphviz filter"
	echo -e "\t-f\n\t--fdp\n\t\tuse \"fdp\" as graphviz filter"
	echo -e "\t-c\n\t--circo\n\t\tuse \"circo\" as graphviz filter"
	echo -e "\t-t format\n\t--type format\n\t\toutput format as accepted by graphviz\n\t\tdefault: $outputFormat"
	echo -e "\t-b\n\t--black\n\t\tdraw the graph in black only (for xdot)"
	echo -e "\t-o filename\n\t--output filename\n\t\tthe name of the file to generate (without extension)\n\t\tdefault: $filename"
	echo -e "\t-w\n\t--write\n\t\twrite the generated graphviz file"
	echo -e "\t-h\n\t-?\n\t--help\n\t\tprint this help"
	echo -e "\nNOTE: if multiple filters are specified, only one of them (usually, the last one) is used; default filter: $graphvizFilter"
}

SHORT_OPTIONS=dfct:bo:wh?
LONG_OPTIONS=dot,fdp,circo,type:,black,output:,write,help
PARAMS=`getopt --options $SHORT_OPTIONS --longoptions $LONG_OPTIONS --name "$0" -- "$@"`

if [[ $? -ne 0 ]]; then
    # e.g. $? == 1
    #  then getopt has complained about wrong arguments to stdout
    exit 2
fi

eval set -- "$PARAMS"

while true; do
    case "$1" in
        -d|--dot)
            graphvizFilter="dot"
            shift
            ;;
        -f|--fdp)
            graphvizFilter="fdp"
            shift
            ;;
        -c|--circo)
            graphvizFilter="circo"
            shift
            ;;
        -t|--type)
            outputFormat="$2"
            shift 2
            ;;
        -o|--output)
            filename="$2"
            shift 2
            ;;
        -w|--write)
            writeFile="1"
            shift
            ;;
        -b|--black)
            onlyBlack="1"
            shift
            ;;
        -h|-\?|--help)
            help
            exit 0
            ;;
        --)
            shift
            break
            ;;
        *)
            echo "Programming error"
            exit 3
            ;;
    esac
done



if [ $onlyBlack == "1" ]; then
	colors=("black")
else
# if wanted, get other colors from http://www.graphviz.org/content/color-names
	colors=("black" "red" "green" "blue" "orange" "cyan" "grey25" "brown" "pink" "purple" "salmon" "aquamarine4" "forestgreen" "darkgoldenrod4" "cadetblue4" "firebrick4" "gold3" "springgreen3" "violetred2" "slateblue1" "purple3" "red3" "palegreen4" "orchid2")
fi

excludes="jani-interaction jani-exporter mpfr guardedcommand-format param predtransform graphsolver-lp graphsolver-iterative specialise-smg specialise-qmc iscasmc-example imdp rddl-format qmc propertysolver-uct cuda propertysolver-ltl-fairness propertysolver-ltl-fg constraintsolver-isat3 constraintsolver-lp-solve constraintsolver-smt-lib dd-beedeedee dd-buddy dd-cacbdd dd-cudd dd-cudd-mtbdd dd-jdd dd-meddly dd-sylvan dd-sylvan-mtbdd"

colorIndex=0
dotContent="digraph dependencies {"
for manifest in `find . -name MANIFEST.MF | grep -v target`; do
	pluginName=`grep "Plugin-Name:" "$manifest" | cut -f2 -d\: | tr -d ' \t\r\n'`
    if [ "$pluginName" = "" ]; then
		continue
    fi
    excluded=0
    for exclude in $excludes; do
		if [ "$exclude" == "$pluginName" ]; then
			excluded=1
			break
		fi
    done
    if [ "$excluded" == "1" ]; then
		continue
    fi
    color=${colors[$colorIndex]}
    colorIndex=$((colorIndex + 1))
    if [ $colorIndex -eq ${#colors[*]} ]; then
		colorIndex=0
	fi
    pluginDependencies=`grep "Plugin-Dependencies:" "$manifest" | cut -f2 -d\: | tr -d '\r\n'`
    for pluginDependency in $pluginDependencies; do
		dotContent="$dotContent\n\t\"$pluginName\" -> \"$pluginDependency\" [color=$color];"
    done
done
dotContent="$dotContent\n}"

echo -e "$dotContent" > "dependency.dot"
if [ "$writeFile" == "1" ]; then
	echo -e "$dotContent" > "$filename".gv
fi
echo -e "$dotContent" | $graphvizFilter -T$outputFormat > "$filename"."$outputFormat"
