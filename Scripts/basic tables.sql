CREATE DATABASE IF NOT EXISTS ACMEMedical;
USE ACMEMedical;

CREATE TABLE medical_school (
    school_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    public BIT(1) NOT NULL DEFAULT 1,
    created DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 1
);

CREATE TABLE medical_training (
    training_id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    active BIT(1) NOT NULL DEFAULT 1,
    created DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 1,
    CONSTRAINT fk_training_school FOREIGN KEY (school_id) REFERENCES medical_school(school_id) ON DELETE CASCADE
);

CREATE TABLE physician (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    created DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 1
);

CREATE TABLE medical_certificate (
    certificate_id INT AUTO_INCREMENT PRIMARY KEY,
    physician_id INT NOT NULL,
    training_id INT NOT NULL,
    signed BIT(1) NOT NULL DEFAULT 0,
    created DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 1,
    CONSTRAINT fk_certificate_physician FOREIGN KEY (physician_id) REFERENCES physician(id) ON DELETE CASCADE,
    CONSTRAINT fk_certificate_training FOREIGN KEY (training_id) REFERENCES medical_training(training_id) ON DELETE CASCADE
);

CREATE TABLE patient (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    year_of_birth INT NOT NULL,
    home_address VARCHAR(100),
    height_cm INT,
    weight_kg INT,
    smoker BIT(1) DEFAULT 0,
    created DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 1
);

CREATE TABLE medicine (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    drug_name VARCHAR(50) NOT NULL,
    manufacturer_name VARCHAR(50) NOT NULL,
    dosage_information VARCHAR(100) NOT NULL,
    created DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 1
);

CREATE TABLE prescription (
    physician_id INT NOT NULL,
    patient_id INT NOT NULL,
    medicine_id INT,
    number_of_refills INT DEFAULT 0,
    prescription_information VARCHAR(100),
    created DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 1,
    PRIMARY KEY (physician_id, patient_id),
    CONSTRAINT fk_prescription_physician FOREIGN KEY (physician_id) REFERENCES physician(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_medicine FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id) ON DELETE SET NULL
);
