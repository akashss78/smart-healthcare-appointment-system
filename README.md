-- =================================================================================
-- Pro Clinic Manager - Database Creation Script (FINAL VERSION with ABHA support)
-- =================================================================================

DROP DATABASE IF EXISTS clinic_management;
CREATE DATABASE clinic_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clinic_management;

--
-- Table for user accounts (admin, doctor, patient)
--
CREATE TABLE `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL, -- Storing plain text as per the original script.
  `role` ENUM('admin', 'doctor', 'patient') NOT NULL
) ENGINE=InnoDB;

--
-- Table for doctor profiles, linked to a user account
--
CREATE TABLE `doctors` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL UNIQUE,
  `name` VARCHAR(100) NOT NULL,
  `specialty` VARCHAR(100) NOT NULL,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

--
-- Table for patient profiles, linked to a user account
--
CREATE TABLE `patients` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL UNIQUE,
  `name` VARCHAR(100) NOT NULL,
  `dob` DATE NOT NULL,
  `phone` VARCHAR(20),
  `abha_id` VARCHAR(255) NULL, -- ADDED: Column to store the patient's ABHA ID.
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

--
-- Table for all appointments
--
CREATE TABLE `appointments` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `patient_id` INT NOT NULL,
  `doctor_id` INT NOT NULL,
  `appointment_datetime` DATETIME NOT NULL,
  `status` ENUM('Scheduled', 'Completed', 'Cancelled') NOT NULL DEFAULT 'Scheduled',
  `notes` TEXT, -- For doctor's diagnosis and prescription notes
  FOREIGN KEY (`patient_id`) REFERENCES `patients`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`doctor_id`) REFERENCES `doctors`(`id`) ON DELETE CASCADE,
  INDEX `idx_appointment_datetime` (`appointment_datetime`)
) ENGINE=InnoDB;

--
-- Table for medical reports and uploaded documents
-- This table links directly to an appointment.
--
CREATE TABLE `medical_reports` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `appointment_id` INT NOT NULL,
  `report_name` VARCHAR(255) NOT NULL, -- e.g., "X-Ray Left Hand", "Blood Test Results"
  `file_path` VARCHAR(512) NOT NULL,   -- The path where the file is stored on the server
  `uploaded_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`appointment_id`) REFERENCES `appointments`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;


-- =================================================================================
-- INSERT INITIAL DATA (ADMIN AND DOCTORS ONLY)
-- =================================================================================
-- SECURITY WARNING: Storing plain text passwords is a severe security risk.

-- 1. Admin User
INSERT INTO `users` (`username`, `password`, `role`) VALUES ('Admin', '12345678', 'admin');

-- 2. Doctor Users and Profiles
-- Mohan
INSERT INTO `users` (`username`, `password`, `role`) VALUES ('dr_mohan', '01', 'doctor');
INSERT INTO `doctors` (`user_id`, `name`, `specialty`) VALUES (LAST_INSERT_ID(), 'Mohan', 'Orthopedist');
-- Anbu
INSERT INTO `users` (`username`, `password`, `role`) VALUES ('dr_anbu', '02', 'doctor');
INSERT INTO `doctors` (`user_id`, `name`, `specialty`) VALUES (LAST_INSERT_ID(), 'Anbu', 'Dermatologist');
-- Adhiyan
INSERT INTO `users` (`username`, `password`, `role`) VALUES ('dr_adhiyan', '03', 'doctor');
INSERT INTO `doctors` (`user_id`, `name`, `specialty`) VALUES (LAST_INSERT_ID(), 'Adhiyan', 'Neurologist');
-- Akila
INSERT INTO `users` (`username`, `password`, `role`) VALUES ('dr_akila', '04', 'doctor');
INSERT INTO `doctors` (`user_id`, `name`, `specialty`) VALUES (LAST_INSERT_ID(), 'Akila', 'Cardiologist');
-- Jovitha
INSERT INTO `users` (`username`, `password`, `role`) VALUES ('dr_jovitha', '05', 'doctor');
INSERT INTO `doctors` (`user_id`, `name`, `specialty`) VALUES (LAST_INSERT_ID(), 'Jovitha', 'General');

-- --- END OF SCRIPT ---
