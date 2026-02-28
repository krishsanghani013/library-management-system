@echo off
setlocal

echo Compiling Library Management System...
if exist out (
    rmdir /s /q out
)
mkdir out

javac -d out src\main\java\library\*.java
if errorlevel 1 (
    echo Compile failed.
    pause
    exit /b 1
)

echo Running application...
java -cp out library.Main
pause
