@echo off
echo ============================================
echo  Expert Reply/Correction Feature Migration
echo ============================================
echo.
echo This will add the ability for experts to reply to
echo or correct other experts' answers.
echo.
echo Features added:
echo - Experts can reply to other experts' answers
echo - Reply types: correction, addition, or general reply
echo - Farmers see both original and reply/corrections
echo - Nested display of expert discussions
echo.
pause

mysql -u root -p agriminds_db < add_expert_reply_feature.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo  Migration completed successfully!
    echo ============================================
    echo.
    echo Changes made:
    echo - Added parent_answer_id column to expert_answers
    echo - Added reply_type column for categorizing replies
    echo - Experts can now comment on each other's answers
    echo - Farmers will see full discussion threads
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
