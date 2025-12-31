package com.agriminds.util;

import com.agriminds.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

/**
 * One-time migration to add question_id and expert_answer_id columns to
 * messages table
 * Run this once to fix the "Unknown column 'question_id'" error
 */
public class MessageTableMigration {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("Running Messages Table Migration...");
        System.out.println("==============================================");

        // Initialize database connection
        DatabaseConnection.initialize();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Add question_id column
            System.out.println("Adding question_id column...");
            stmt.execute("ALTER TABLE messages ADD COLUMN question_id BIGINT NULL AFTER expert_id");
            System.out.println("✓ question_id column added");

            // Add expert_answer_id column
            System.out.println("Adding expert_answer_id column...");
            stmt.execute("ALTER TABLE messages ADD COLUMN expert_answer_id BIGINT NULL AFTER question_id");
            System.out.println("✓ expert_answer_id column added");

            // Add indexes
            System.out.println("Adding indexes...");
            stmt.execute("ALTER TABLE messages ADD INDEX idx_question (question_id)");
            stmt.execute("ALTER TABLE messages ADD INDEX idx_answer (expert_answer_id)");
            System.out.println("✓ Indexes added");

            // Add foreign keys
            System.out.println("Adding foreign key constraints...");
            stmt.execute(
                    "ALTER TABLE messages ADD CONSTRAINT fk_message_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE");
            stmt.execute(
                    "ALTER TABLE messages ADD CONSTRAINT fk_message_expert_answer FOREIGN KEY (expert_answer_id) REFERENCES expert_answers(id) ON DELETE CASCADE");
            System.out.println("✓ Foreign keys added");

            System.out.println("\n==============================================");
            System.out.println("SUCCESS! Migration completed successfully!");
            System.out.println("You can now send messages in the application.");
            System.out.println("==============================================");

        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate column name")) {
                System.out.println("\n==============================================");
                System.out.println("Columns already exist - migration not needed!");
                System.out.println("Your database is already up to date.");
                System.out.println("==============================================");
            } else {
                System.err.println("\nERROR during migration:");
                e.printStackTrace();
            }
        }
    }
}
