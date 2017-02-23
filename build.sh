# written by LI. Yi
# created on Feb 23rd, 2017

# building step 1. apply platform-specific settings
platform=$(uname -s)

ROOT="$( cd "$( dirname "$0"  )" && pwd  )"

case $platform in
    Darwin)
        # backup platform-specific settings
        cp $ROOT/native.properties $ROOT/building_scripts/native.properties.backup
        # use default settings for this platform
        cp $ROOT/building_scripts/native.properties.Darwin $ROOT/native.properties 
    ;;
    # ...
esac

# build core package
cd $ROOT
mvn package

# build plugins
cd $ROOT/plugins
sh build.sh

# build distributions
cd $ROOT/distributions/$1
sh build.sh
cp epmc-$1.jar $ROOT/
cd $ROOT
