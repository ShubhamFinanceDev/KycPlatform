-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.35 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.6.0.6765
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for rekyc
CREATE DATABASE IF NOT EXISTS `rekyc` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `rekyc`;

-- Dumping structure for table rekyc.customer_details
CREATE TABLE IF NOT EXISTS `customer_details` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `application_number` varchar(255) DEFAULT NULL,
  `loan_number` varchar(20) DEFAULT NULL,
  `customer_name` varchar(255) DEFAULT NULL,
  `address_details_residential` varchar(255) DEFAULT NULL,
  `mobile_number` varchar(10) DEFAULT NULL,
  `PAN` varchar(12) DEFAULT NULL,
  `Aadhar` bigint DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table rekyc.customer_details: ~2 rows (approximately)
INSERT INTO `customer_details` (`user_id`, `application_number`, `loan_number`, `customer_name`, `address_details_residential`, `mobile_number`, `PAN`, `Aadhar`) VALUES
	(1, '98765', 'LN003', 'nainish', '34 Main St, City', '8218372914', 'ABCDE1234F', 889831516204),
	(2, '76756', 'LN005', 'kajal', '90 Main St, City', '8840961736', 'ABCuE1234F', 390920211147);

-- Dumping structure for table rekyc.customer_details_seq
CREATE TABLE IF NOT EXISTS `customer_details_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table rekyc.customer_details_seq: ~1 rows (approximately)
INSERT INTO `customer_details_seq` (`next_val`) VALUES
	(1);

-- Dumping structure for table rekyc.otp_detail
CREATE TABLE IF NOT EXISTS `otp_detail` (
  `expr_time` datetime(6) DEFAULT NULL,
  `mobile_number` varchar(255) DEFAULT NULL,
  `otp_code` bigint DEFAULT NULL,
  `otp_id` bigint NOT NULL,
  `otp_password` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`otp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table rekyc.otp_detail: ~3 rows (approximately)
INSERT INTO `otp_detail` (`expr_time`, `mobile_number`, `otp_code`, `otp_id`, `otp_password`) VALUES
	('2024-01-09 18:35:59.000000', '8755776778', 590374, 602, NULL),
	('2024-01-13 22:58:12.000000', NULL, 366009, 702, NULL),
	('2024-01-15 20:15:24.000000', '8840961736', 961616, 852, '$2a$10$7slGtkQ.LojUuTuHhwrNQOW1kArQviSF6L3pxlg1Ns2O83jpR5wvS');

-- Dumping structure for table rekyc.otp_detail_seq
CREATE TABLE IF NOT EXISTS `otp_detail_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table rekyc.otp_detail_seq: ~1 rows (approximately)
INSERT INTO `otp_detail_seq` (`next_val`) VALUES
	(951);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
