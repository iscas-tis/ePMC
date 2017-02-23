# written by LI. Yi
# created on Feb 23rd, 2017
ROOT="$( cd "$( dirname "$0"  )" && pwd  )"

# building step 1. apply platform-specific settings
platform=$(uname -s)

# backup platform-specific settings
cp $ROOT/native.properties $ROOT/building_scripts/native.properties.backup

case $platform in
    Darwin)
        # use default settings for this platform
        cp $ROOT/building_scripts/native.properties.Darwin $ROOT/native.properties 
    ;;
    Linux)
        # check if the system is 32-bits or 64-bits
        bits=$(getconf LONG_BIT)
        cp $ROOT/building_scripts/native.properties.Linux.$bits $ROOT/native.properties
    ;;
esac

# build core package
cd $ROOT
mvn package

# build plugins
cd $ROOT/plugins
sh build.sh

# build distributions
if [ "$1" == "" ]; then
    dist=standard
else
    dist=$1
fi

cd $ROOT/distributions/$dist
sh build.sh
cp epmc-$dist.jar $ROOT/
cd $ROOT

echo "ePMC distribution <$dist> has been packaged successfully!"
echo "the target jar file is located in $ROOT/epmc-$dist.jar"
echo "start your ePMC trip with the following line:\n"
echo "    java -jar epmc-$dist.jar help"
echo "\n"
echo "enjoy!"
