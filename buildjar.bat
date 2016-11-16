@echo off

if exist bin rmdir bin /q /s
mkdir bin

javac src\cyoencrypt\*.java -d bin
if errorlevel 1 goto:eof

cd bin
jar cf ..\cyoencrypt.jar cyoencrypt\*.class
cd ..
