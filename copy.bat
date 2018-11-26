@echo off
xcopy L:\Doc\SVN\Work\XC2002\src\XC2002WB\app\src\main L:\Doc\Git\AppInvXC2002Wb\app\src\main\ /S
xcopy L:\Doc\SVN\Work\XC2002\src\XC2002WB\app\libs L:\Doc\Git\AppInvXC2002Wb\app\libs\ /S
copy L:\Doc\SVN\Work\XC2002\src\XC2002WB\app\build.gradle L:\Doc\Git\AppInvXC2002Wb\app
pause
