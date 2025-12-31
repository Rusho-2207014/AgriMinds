@echo off
echo Adding acceptance column to expert_answers table...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pRushorkr@gmail.com agriminds_db < add_correction_acceptance.sql
echo Done!
pause
