@echo off

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe

cd %~dp0

set APP_HOME=%~dp0
set APP_HOME=%APP_HOME:\bin\=%
set LOCALCLASSPATH=%CLASSPATH%

if ""%1"" == ""crypt"" goto doCrypt
if ""%1"" == ""md5"" goto doMD5

echo Usage: crypt scheme password
echo scheme:
echo  crypt            Traditional DES-crypted password
echo    md5            MD5 based salted password
goto end

:doCrypt
shift
set ENCODER="com.hs.mail.security.login.CryptPasswordEncoder"
goto execCmd

:doMD5
shift
set ENCODER="com.hs.mail.security.login.MD5PasswordEncoder"

:execCmd
"%_JAVACMD%" -classpath "%LOCALCLASSPATH%" -jar "%APP_HOME%\bin\run.jar" %ENCODER% %1
goto end

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo.

:end
set LOCALCLASSPATH=
set _JAVACMD=

