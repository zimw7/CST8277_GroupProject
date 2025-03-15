-- This might be useful
USE `acmemedical`;
ALTER TABLE physician AUTO_INCREMENT = 1;
ALTER TABLE medical_school AUTO_INCREMENT = 1;
ALTER TABLE medical_training AUTO_INCREMENT = 1;
ALTER TABLE medical_certificate AUTO_INCREMENT = 1;
ALTER TABLE medicine AUTO_INCREMENT = 1;
ALTER TABLE patient AUTO_INCREMENT = 1;
-- Note:  No auto_increment on prescription table as it has a composite primary key