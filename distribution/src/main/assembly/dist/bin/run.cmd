@echo off
rem -------------------------------------------------------------------------
rem ${app.name.pretty} Bootstrap Script for Windows
rem -------------------------------------------------------------------------

set "DIRNAME=%~dp0%"

rem Setup APP_HOME
pushd "%DIRNAME%.."
set "APP_HOME=%CD%"
popd

rem Read an optional configuration file.
if "x%RUN_CONF%" == "x" (
   set "APP_CONF=%DIRNAME%run.conf.bat"
)
if exist "%APP_CONF%" (
   echo Calling "%APP_CONF%"
   call "%APP_CONF%" %*
) else (
   echo Config file not found "%APP_CONF%"
)

rem Setup the JVM
if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  if not exist "%JAVA_HOME%" (
    echo JAVA_HOME "%JAVA_HOME%" path doesn't exist
    goto END
  ) else (
    echo Setting JAVA property to "%JAVA_HOME%\bin\java"
    set "JAVA=%JAVA_HOME%\bin\java"
  )
)

rem Display our environment
echo ===============================================================================
echo.
echo   ${app.name.pretty} Bootstrap Environment
echo.
echo   Home Directory: "%APP_HOME%"
echo.
echo   JAVA: "%JAVA%"
echo.
echo   JAVA_OPTS: "%JAVA_OPTS%"
echo.
echo ===============================================================================
echo.

"%JAVA%" %JAVA_OPTS% ^
	-Djava.io.tmpdir=%APP_HOME%\temp\ ^
	-Dlogging.config=%APP_HOME%\config\logback-spring.xml -Dlogging.path=%APP_HOME%\logs ^
	-jar %APP_HOME%\lib\${app.name.id}.jar ^
	--spring.config.additional-location=%APP_HOME%\config\
