@echo off
setlocal
if not defined MAKE_EXE set "MAKE_EXE=mingw32-make.exe"
if not defined SH_EXE set "SH_EXE=sh.exe"
"%MAKE_EXE%" SHELL="%SH_EXE%" %*
exit /b %ERRORLEVEL%
