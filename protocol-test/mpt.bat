@echo off

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe

cd %~dp0

"%_JAVACMD%" -jar "apache-james-mpt-app-0.1.jar" -p 143 -f %1
goto end

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo.

:end
set LOCALCLASSPATH=
set _JAVACMD=

