@echo off
java -jar "%~dp0resources\lib\musescore2html.jar" %*
if NOT ["%errorlevel%"]==["0"] (
	pause
	exit /b %errorlevel%
)