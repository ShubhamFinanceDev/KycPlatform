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
	(1, '98765', 'LN003', 'nainish', '34 Main St, City', '8218372914', 'ABCDE1234F', 123456789012),
	(2, '76756', 'LN005', 'kajal', '90 Main St, City', '8755776778', 'ABCuE1234F', 390920211147);

-- Dumping structure for table rekyc.customer_details_seq
CREATE TABLE IF NOT EXISTS `customer_details_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table rekyc.customer_details_seq: ~1 rows (approximately)
INSERT INTO `customer_details_seq` (`next_val`) VALUES
	(1);

-- Dumping structure for table rekyc.otp_detail
CREATE TABLE IF NOT EXISTS `otp_detail` (
  `otp_id` bigint NOT NULL AUTO_INCREMENT,
  `expr_time` datetime(6) DEFAULT NULL,
  `mobile_number` varchar(255) DEFAULT NULL,
  `otp_code` bigint DEFAULT NULL,
  PRIMARY KEY (`otp_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2404 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table rekyc.otp_detail: ~2 rows (approximately)
INSERT INTO `otp_detail` (`otp_id`, `expr_time`, `mobile_number`, `otp_code`) VALUES
	(552, '2024-01-18 16:40:47.343530', '8218372914', 610536),
	(2403, '2024-01-23 13:26:27.875211', '8755776778', 198934);

-- Dumping structure for table rekyc.otp_detail_seq
CREATE TABLE IF NOT EXISTS `otp_detail_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table rekyc.otp_detail_seq: ~1 rows (approximately)
INSERT INTO `otp_detail_seq` (`next_val`) VALUES
	(2501);

-- Dumping structure for table rekyc.updated_details
CREATE TABLE IF NOT EXISTS `updated_details` (
  `update_id` bigint NOT NULL AUTO_INCREMENT,
  `loan_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `document_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `document_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `updated_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mobile_no` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `updated_date` date DEFAULT NULL,
  PRIMARY KEY (`update_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table rekyc.updated_details: ~41 rows (approximately)
INSERT INTO `updated_details` (`update_id`, `loan_no`, `document_id`, `document_type`, `updated_address`, `mobile_no`, `updated_date`) VALUES
	(1, '', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(2, '', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(3, 'LN005', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(4, 'LN005', '', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(5, 'LN005', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(6, 'LN005', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(7, 'LN005', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(8, 'LN005', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(9, 'LN003', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8218372914', NULL),
	(10, 'LN003', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8218372914', NULL),
	(11, 'LN003', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8218372914', NULL),
	(12, 'LN005', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(13, 'LN005', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(14, 'LN005', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', NULL),
	(15, 'LN005', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', '2024-01-18'),
	(16, 'LN005', '390920211147', 'aadhar', 'S / O : Rakesh Singh , 75 , ward  no,01 , kundesar , kundesar , Kundaser , Ghazipur , Uttar Pradesh - 233227', '8755776778', '2024-01-18'),
	(17, 'string', 'string', 'string', 'string', 'string', '2024-01-20'),
	(18, 'string', 'string', 'string', 'string', 'string', '2024-01-20'),
	(19, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-20'),
	(20, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-20'),
	(21, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-20'),
	(22, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-20'),
	(23, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(24, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(25, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(26, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(27, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(28, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(29, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(30, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(31, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(32, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(33, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(34, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(35, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(36, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(37, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(38, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(39, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(40, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21'),
	(41, 'LN005', '390920211147', 'aadhar', 'SCn,hihuihdkvczz', '8755776778', '2024-01-21');

-- Dumping structure for table rekyc.uploaded_document
CREATE TABLE IF NOT EXISTS `uploaded_document` (
  `upload_id` bigint NOT NULL AUTO_INCREMENT,
  `loan_no` varchar(255) DEFAULT NULL,
  `document_id` varchar(255) DEFAULT NULL,
  `document_type` varchar(255) DEFAULT NULL,
  `front` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `back` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `updated_date` date DEFAULT NULL,
  PRIMARY KEY (`upload_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table rekyc.uploaded_document: ~0 rows (approximately)

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
