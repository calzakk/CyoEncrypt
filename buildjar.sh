#!/bin/sh

set -e

rm -rf bin
mkdir bin

javac src/*.java -d bin

cd bin
jar cf ../cyoencrypt.jar cyoencrypt/*.class
cd ..
