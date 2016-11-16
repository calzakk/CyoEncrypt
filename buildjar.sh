#!/bin/sh

set -e

rm -rf bin
mkdir bin

javac src/cyoencrypt/*.java -d bin

cd bin
jar cf ../cyoencrypt.jar cyoencrypt/*.class
cd ..
