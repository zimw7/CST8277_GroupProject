USE `acmemedical`;

-- MySQL dump 10.13  Distrib 8.0.23, for Win64 (x86_64)

-- data for table `medicine`
INSERT INTO `medicine` (`medicine_id`, `drug_name`, `manufacturer_name`, `dosage_information`, `created`, `updated`) 
  VALUES (1,'Tylenol','McNeil Laboratories','Take 2 tablets per day',now(), now());

-- data for table `physician`
INSERT INTO `physician` (`id`, `first_name`, `last_name`,  `created`, `updated`)
  VALUES (1,'John','Smith',now(),now());

-- data for table `patient`
INSERT INTO `patient` (`patient_id`, `first_name`, `last_name`, `year_of_birth`, `home_address`, `height_cm`, `weight_kg`, `smoker`, `created`, `updated`)
  VALUES (1,'Mary','Brown',1970,'123 Bank St. Ottawa',152,60,0,now(),now()),(2,'Charlie','Lee',1980,'999 Sunset Blvd. Beverly Hills',183,80,1,now(),now());

-- data for table `medical_school`
INSERT INTO `medical_school` (`school_id`, `name`, `public`,`created`, `updated`)
  VALUES (1,'University of California Medical School',1,now(),now()),(2,'John Hopkins Medical School',0,now(),now());

-- data for table `medical_training`
INSERT INTO `medical_training` (`training_id`, `school_id`, `start_date`, `end_date`, `active`, `created`, `updated`)
  VALUES (1,2,now(),now(),1,now(),now()),(2,2,now(),now(),0,now(),now());

-- data for table `prescription`
INSERT INTO `prescription` (`physician_id`, `patient_id`, `number_of_refills`, `prescription_information`, `medicine_id`, `created`, `updated`)
  VALUES (1,1,10,'Take after every meal for 2 weeks',1,now(),now()),(1,2,5,NULL,NULL,now(),now());

-- data for table `medical_certificate`
INSERT INTO `medical_certificate` (`certificate_id`, `physician_id`, `training_id`, `signed`, `created`, `updated`)
  VALUES (1,1,1,1,now(),now()),(2,1,2,0,now(),now());

-- data for table `security_role`
INSERT INTO `security_role` (`role_id`, `name`)
  VALUES (1,'ADMIN_ROLE'), (2,'USER_ROLE');

-- data for table `security_user`
-- value for `password_hash` column computed by PBKDF2HashGenerator
--   user 'admin', password 'admin'
--   user 'cst8277', password '8277'
INSERT INTO `security_user` (`user_id`, `password_hash`, `username`, `physician_id`)
  VALUES (1, 'PBKDF2WithHmacSHA256:2048:hYKwYbuwalL2mbXT3Lx8QgJuTWT8GgZcGljMPEW+TZA=:6GmiBW47QsKVgqF7wzt/wjQAMDd0RVMok3M8WPu8Y1U=', 'admin', null), (2, 'PBKDF2WithHmacSHA256:2048:ZJC4ipE7LQOZzOQyd2ch7VOxHJWwrVfDFTbo9H+U5Fw=:j5Wulo/tVmolv8hqu0k5ejTOPEMbzviQXStg/0/c6Qo=', 'cst8277', 1);

--  data for table `user_has_role`
INSERT INTO `user_has_role` (`user_id`, `role_id`)
  VALUES (1,1), (2,2);
