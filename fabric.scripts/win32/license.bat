@echo OFF
rem **************************************************************************
rem (C) Copyright IBM Corp. 2014
rem
rem LICENSE: Eclipse Public License v1.0
rem http://www.eclipse.org/legal/epl-v10.html
rem **************************************************************************

rem **************************************************************************
rem Display license and confirm acceptance.
rem **************************************************************************

set licenseAccepted=tbd

echo You must read and accept the license agreement for this software before installing and using it.
set /p continue="Press enter to display the license agreement."
  
set licenseFile=LICENSE.txt
if not exist %licenseFile% goto :nolicense

more %licenseFile%
echo.

rem Loop until the user has accepted or rejected the license...
:repeat

    set reply=tbd
    set /p reply=Do you accept the license agreement (yes or no)? 

    if "%reply%" == "yes" set licenseAccepted=y
    if "%reply%" == "no" set licenseAccepted=n

    if "%licenseAccepted%" == "tbd" goto :repeat 
    goto :end

:nolicense
  
echo.
echo Error: cannot open the license file (%licenseFile%).
echo Please contact your support representative.
set licenseAccepted=n

:end

rem If the license was accepted...
if "%licenseAccepted%" == "y" exit /b 0
exit /b 1
