@echo off
echo Stopping MySQL service...
net stop MySQL80

echo.
echo Starting MySQL in reset mode...
start /B "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --init-file="%~dp0reset-mysql-password.txt" --console

echo.
echo Waiting 10 seconds for MySQL to start...
timeout /t 10 /nobreak

echo.
echo Stopping MySQL process...
taskkill /F /IM mysqld.exe

echo.
echo Starting MySQL service normally...
net start MySQL80

echo.
echo Done! Your new password is: agriminds123
pause
