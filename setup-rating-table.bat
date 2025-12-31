@echo off
echo Creating expert_ratings table...
echo.
echo Please enter your MySQL root password when prompted.
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p agriminds_db -e "CREATE TABLE IF NOT EXISTS expert_ratings (id BIGINT AUTO_INCREMENT PRIMARY KEY, expert_id BIGINT NOT NULL, farmer_id BIGINT NOT NULL, expert_answer_id BIGINT NOT NULL, rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5), comment TEXT, rated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (expert_id) REFERENCES experts(id) ON DELETE CASCADE, FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE, FOREIGN KEY (expert_answer_id) REFERENCES expert_answers(id) ON DELETE CASCADE, UNIQUE KEY unique_farmer_answer (farmer_id, expert_answer_id), INDEX idx_expert (expert_id), INDEX idx_farmer (farmer_id), INDEX idx_rating (rating)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS! expert_ratings table created successfully.
    echo Rating system is now fully operational!
    echo.
) else (
    echo.
    echo ERROR: Failed to create table. Please check your MySQL password.
    echo.
)

pause
