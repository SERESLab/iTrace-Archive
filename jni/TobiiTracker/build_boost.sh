#!/bin/bash

if [ ! -d build ]
then
    mkdir build
fi
cd build

USING_MSYS=`uname -a | grep -q "Msys"`
echo $USING_MSYS

which wget
WGET_EXISTS=$?

if [ "$USING_MSYS" != "" ] && [ "$WGET_EXISTS" -ne 0 ]
then
    mingw-get install msys-wget-bin
fi

if [ ! -f boost_1_40_0.tar.gz ]
then
    wget -O boost_1_40_0.tar.gz http://downloads.sourceforge.net/project/boost/boost/1.40.0/boost_1_40_0.tar.gz?use_mirror=kent
fi

if [ ! -d boost_1_40_0 ]
then
    tar -xzf boost_1_40_0.tar.gz
fi
cd boost_1_40_0
if [ "$USING_MSYS" != "" ]
then
    ./bootstrap.sh --with-toolset=mingw
    sed -i -e's/mingw/gcc/' project-config.jam
else
    ./bootstrap.sh
fi
./bjam --toolset=gcc --layout=tagged --build-type=complete --exec-prefix=/usr/local --with-system --with-filesystem --with-thread --with-regex install link=shared -j 5 install
cd ../../
