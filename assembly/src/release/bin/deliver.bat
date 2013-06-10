@echo off

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe

cd %~dp0

set APP_HOME=%~dp0
set APP_HOME=%APP_HOME:\bin\=%
set LOCALCLASSPATH=%CLASSPATH%

if "%DELIVER_OPTS%" == "" set DELIVER_OPTS=-Dlog4j.configuration="file:..\conf\log4j.properties"
set RUN_TASK="com.hs.mail.deliver.Deliver"
"%_JAVACMD%" -classpath "%LOCALCLASSPATH%" %DELIVER_OPTS% -jar "%APP_HOME%\bin\run.jar" %RUN_TASK% -c "..\conf\default.properties" -p %1
goto end

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo.

:end
set LOCALCLASSPATH=
set _JAVACMD=
