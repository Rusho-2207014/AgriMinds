@echo off
echo Updating messages table with question_id and expert_answer_id columns...
echo.
echo Please enter your MySQL root password when prompted.
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p agriminds_db < add_message_context_columns.sql

if %errorlevel% equ 0 (
    echo.
    echo SUCCESS: Messages table updated successfully!
    echo You can now send messages with answer context.
) else (
    echo.
    echo ERROR: Failed to update table. Please check:
    echo 1. MySQL is installed at C:\Program Files\MySQL\MySQL Server 8.0\
    echo 2. Your password is correct
    echo 3. The database agriminds_db exists
)

pause
