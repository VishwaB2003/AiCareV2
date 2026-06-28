-- AiCare Database Schema
-- Run once to create the database (Hibernate handles table creation via ddl-auto=update)

CREATE DATABASE IF NOT EXISTS aicare_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE aicare_db;

-- Users table (created automatically by Hibernate, shown here for reference)
-- CREATE TABLE IF NOT EXISTS users (
--     id         BIGINT AUTO_INCREMENT PRIMARY KEY,
--     first_name VARCHAR(50)  NOT NULL,
--     last_name  VARCHAR(50)  NOT NULL,
--     email      VARCHAR(100) NOT NULL UNIQUE,
--     password   VARCHAR(255) NOT NULL,
--     created_at DATETIME     DEFAULT CURRENT_TIMESTAMP
-- );
