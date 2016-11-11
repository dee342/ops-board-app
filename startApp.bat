@echo off
set TOMCATPORT=%1
echo Configured tomcat port: %TOMCATPORT%
call gradlew.bat bootRun 
