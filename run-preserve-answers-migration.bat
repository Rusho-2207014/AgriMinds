@echo off
echo ============================================
echo  Preserve Expert Answers Migration
echo ============================================
echo.
echo This will update the expert_answers table to preserve
echo answer records even when farmers delete questions.
echo.
echo This allows tracking of how many questions an expert
echo has answered, regardless of whether the questions
echo have been deleted.
echo.
pause

mysql -u root -p agriminds_db < preserve_expert_answers.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo  Migration completed successfully!
    echo ============================================
    echo.
    echo Changes made:
    echo - question_id in expert_answers is now nullable
    echo - Foreign key changed to ON DELETE SET NULL
    echo - Expert answer counts will include deleted questions
    echo.
) else (
    echo.
    echo ============================================
    echo  Migration FAILED!
    echo ============================================
    echo.
    echo Please check the error messages above.
    echo.
)

pause
