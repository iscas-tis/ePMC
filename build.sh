#!/bin/bash
# written by LI. Yi
# created on Feb 23rd, 2017
ROOT="$( cd "$( dirname "$0"  )" && pwd  )"

# building step 1. apply platform-specific settings
platform=$(uname -s)

echo $ROOT
# backup platform-specific settings
cp "$ROOT/native.properties" "$ROOT/building_scripts/native.properties.backup"

case $platform in
    Darwin)
        # use default settings for this platform
        cp "$ROOT/building_scripts/native.properties.Darwin" "$ROOT/native.properties" 
    ;;
    Linux)
        # check if the system is 32-bits or 64-bits
        bits=$(getconf LONG_BIT)
        cp "$ROOT/building_scripts/native.properties.Linux.$bits" "$ROOT/native.properties"
    ;;
    *)
        # TODO currently the script only support 64 bits windows system
        cp "$ROOT/building_scripts/native.properties.Windows.64" "$ROOT/native.properties"
esac

# build core package
cd "$ROOT"
mvn package

# build plugins
cd "$ROOT/plugins"
bash build.sh

# build distributions
if [ "$1" == "" ]; then
    dist=standard
else
    dist=$1
fi

cd "$ROOT/distributions/$dist"
bash build.sh
cp epmc-$dist.jar "$ROOT/"
cd "$ROOT"

echo "ePMC distribution <$dist> has been packaged successfully!"
echo "the target jar file is located in $ROOT/epmc-$dist.jar"
echo "start your ePMC trip with the following line:"
echo ""
echo "    java -jar epmc-$dist.jar help"
echo ""
echo "enjoy!"
