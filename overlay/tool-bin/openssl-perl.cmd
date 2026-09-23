@echo off
setlocal
if not defined PERL_EXE set "PERL_EXE=perl.exe"
"%PERL_EXE%" %*
exit /b %ERRORLEVEL%
