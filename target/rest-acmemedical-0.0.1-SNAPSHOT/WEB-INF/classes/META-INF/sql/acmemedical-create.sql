-- -----------------------------------------------------
-- Drop Schema for ACMEMedical Application
--
-- In order for the `cst8277`@`localhost` user to be able to create (or drop) a schema,
-- it needs additional privileges.  If you are using MySQL Workbench, log-in to it as root,
-- click on the 'Administration' tab, select 'Users and Privileges' and find and click the cst8277 user.
-- The 'Administrative Roles' tab has an entry 'DBA' - select it, and click all the individual PRIVILEGES
-- and then click 'Apply'.
--
-- If you wish to use a 'raw' .sql-script instead, you still need to log-in as root,
-- the command to GRANT the appropriate PRIVILEGES is:
-- GRANT ALL PRIVILEGES ON *.* TO `cst8277`@`localhost`;
--
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `acmemedical` DEFAULT CHARACTER SET utf8mb4;
USE `acmemedical`;

-- ------------------------------------------------------------------------
-- Table `physician`
-- ------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `physician` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(50) NOT NULL,
  `last_name` VARCHAR(50) NOT NULL,
  `created` DATETIME NULL,
  `updated` DATETIME NULL,
  `version` BIGINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
);

-- -----------------------------------------------------
-- Table `medical_school`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `medical_school` (
  `school_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `public` BIT(1) NOT NULL,
  `created` DATETIME NULL,
  `updated` DATETIME NULL,
  `version` BIGINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`school_id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE
);

-- -----------------------------------------------------
-- Table `medical_training`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `medical_training` (
  `training_id` INT NOT NULL AUTO_INCREMENT,
  `school_id` INT NOT NULL,
  `start_date` DATETIME NOT NULL,
  `end_date` DATETIME NOT NULL,
  `active` BIT(1) NOT NULL,
  `created` DATETIME NULL,
  `updated` DATETIME NULL,
  `version` BIGINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`training_id`),
  INDEX `fk_medical_training_medical_school_idx` (`school_id` ASC) VISIBLE,
  CONSTRAINT `fk_medical_training_medical_school1`
    FOREIGN KEY (`school_id`) REFERENCES `medical_school` (`school_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `medical_certificate`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `medical_certificate` (
  `certificate_id` INT NOT NULL AUTO_INCREMENT,
  `physician_id` INT NOT NULL,
  `training_id` INT NULL,
  `signed` BIT(1) NOT NULL,
  `created` DATETIME NULL,
  `updated` DATETIME NULL,
  `version` BIGINT NOT NULL DEFAULT 1,
  INDEX `fk_medical_certificate_physician1_idx` (`physician_id` ASC) VISIBLE,
  INDEX `fk_medical_certificate_medical_training_idx` (`training_id` ASC) VISIBLE,
  UNIQUE INDEX `certificate_id_UNIQUE` (`certificate_id` ASC) VISIBLE,
  PRIMARY KEY (`certificate_id`),
  CONSTRAINT `fk_medical_certificate_physician1`
    FOREIGN KEY (`physician_id`)
    REFERENCES `physician` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_medical_certificate_medical_training1`
    FOREIGN KEY (`training_id`)
    REFERENCES `medical_training` (`training_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `medicine`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `medicine` (
  `medicine_id` INT NOT NULL AUTO_INCREMENT,
  `drug_name` VARCHAR(50) NOT NULL,
  `manufacturer_name` VARCHAR(50) NOT NULL,
  `dosage_information` VARCHAR(100) NOT NULL,
  `created` DATETIME NULL,
  `updated` DATETIME NULL,
  `version` BIGINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`medicine_id`)
);

-- -----------------------------------------------------
-- Table `patient`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `patient` (
  `patient_id` INT NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(50) NOT NULL,
  `last_name` VARCHAR(50) NOT NULL,
  `year_of_birth` INT NOT NULL,
  `home_address` VARCHAR(100) NOT NULL,
  `height_cm` INT NOT NULL,
  `weight_kg` INT NOT NULL,
  `smoker` BIT(1) NOT NULL,
  `created` DATETIME NULL,
  `updated` DATETIME NULL,
  `version` BIGINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`patient_id`)
);

-- -----------------------------------------------------
-- Table `prescription`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `prescription` (
  `physician_id` INT NOT NULL,
  `patient_id` INT NOT NULL,
  `number_of_refills` INT NULL,
  `prescription_information` VARCHAR(100) NULL,
  `medicine_id` INT NULL,
  `created` DATETIME NULL,
  `updated` DATETIME NULL,
  `version` BIGINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`physician_id`, `patient_id`),
  INDEX `fk_prescription_patient1_idx` (`patient_id` ASC) VISIBLE,
  INDEX `fk_prescription_physician1_idx` (`physician_id` ASC) VISIBLE,
  CONSTRAINT `fk_prescription_medicine1`
    FOREIGN KEY (`medicine_id`)
    REFERENCES `medicine` (`medicine_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_prescription_patient1`
    FOREIGN KEY (`patient_id`)
    REFERENCES `patient` (`patient_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_prescription_physician1`
    FOREIGN KEY (`physician_id`)
    REFERENCES `physician` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- Table for SecurityUser

CREATE TABLE IF NOT EXISTS `security_user` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `password_hash` VARCHAR(256) NOT NULL,
  `username` VARCHAR(100) NOT NULL,
  `physician_id` INT NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE INDEX `user_id_UNIQUE` (`user_id` ASC) VISIBLE,
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE,
  INDEX `fk_security_user_physician1_idx` (`physician_id` ASC) VISIBLE,
  CONSTRAINT `fk_security_user_physician1`
    FOREIGN KEY (`physician_id`)
    REFERENCES `physician` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);


-- Table for SecurityRole

CREATE TABLE IF NOT EXISTS `security_role` (
  `role_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE INDEX `role_id_UNIQUE` (`role_id` ASC) VISIBLE,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE
);


-- Table for the Many-to-Many relationship between SecurityUser and SecurityRole
CREATE TABLE IF NOT EXISTS `user_has_role` (
  `user_id` INT NOT NULL,
  `role_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  INDEX `fk_security_user_has_security_role_security_role1_idx` (`role_id` ASC) VISIBLE,
  INDEX `fk_security_user_has_security_role_security_user1_idx` (`user_id` ASC) VISIBLE,
  CONSTRAINT `fk_security_user_has_security_role_security_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `security_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_security_user_has_security_role_security_role1`
    FOREIGN KEY (`role_id`)
    REFERENCES `security_role` (`role_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);
