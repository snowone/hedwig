@echo off

set JAVA_OPTS=%JAVA_OPTS% -Xms256m -Xmx256m

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe

cd %~dp0

set APP_HOME=%~dp0
set APP_HOME=%APP_HOME:\bin\=%

echo %APP_HOME%
set _LIBJARS=..\classes
set JARS_DIR=..\lib
for %%i in ("%JARS_DIR%\*.jar") do call ".\cpappend.bat" %%i

echo JAVA_HOME: %JAVA_HOME%
echo CLASSPATH: %_LIBJARS%
echo Application starts...
"%_JAVACMD%" -classpath %_LIBJARS% %JAVA_OPTS% "-Dlog4j.configuration=file:%APP_HOME%\conf\log4j.properties" com.hs.mail.container.simple.SimpleSpringContainer -c "%APP_HOME%\conf\applicationContext.xml"
goto end

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo   If build fails because sun.* classes could not be found
echo   you will need to set the JAVA_HOME environment variable
echo   to the installation directory of java.
echo.

:end
set _LIBJARS=
set _JAVACMD=
