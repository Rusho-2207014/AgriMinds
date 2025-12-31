package com.agriminds.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Migration to enable expert-to-expert messaging by making farmer_id nullable
 * Run this once to update the database schema
 */
public class EnableExpertToExpertMessaging {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/agriminds_db";
        String username = "root";
        String password = "Rushorkr@gmail.com"; // Update if different

        System.out.println("Starting migration to enable expert-to-expert messaging...");

        try (Connection conn = DriverManager.getConnection(url, username, password);
                Statement stmt = conn.createStatement()) {

            // Make farmer_id nullable
            System.out.println("Making farmer_id column nullable...");
            stmt.execute("ALTER TABLE messages MODIFY COLUMN farmer_id BIGINT NULL");
            System.out.println("✓ farmer_id is now nullable");

            System.out.println("\n=== SUCCESS! Expert-to-expert messaging is now enabled.");
            System.out.println("Experts can now message each other directly.");

        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate column name") ||
                    e.getMessage().contains("already exists")) {
                System.out.println("Migration already applied - farmer_id is already nullable!");
            } else {
                System.err.println("=== ERROR during migration:");
                e.printStackTrace();
            }
        }
    }
}
