/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : medication_tracker

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 03/01/2026 11:48:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for check_item
-- ----------------------------
DROP TABLE IF EXISTS `check_item`;
CREATE TABLE `check_item` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `name` varchar(50) NOT NULL COMMENT '检查项名称',
                              `unit` varchar(20) DEFAULT NULL COMMENT '单位',
                              `reference_min` decimal(10,2) DEFAULT NULL COMMENT '参考范围下限',
                              `reference_max` decimal(10,2) DEFAULT NULL COMMENT '参考范围上限',
                              `is_preset` tinyint DEFAULT '0' COMMENT '是否预设项目',
                              `sort_order` int DEFAULT '0' COMMENT '排序',
                              `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检查项目表';

-- ----------------------------
-- Table structure for check_record
-- ----------------------------
DROP TABLE IF EXISTS `check_record`;
CREATE TABLE `check_record` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `check_item_id` bigint NOT NULL COMMENT '检查项ID',
                                `check_date` date NOT NULL COMMENT '检查日期',
                                `value` decimal(10,2) NOT NULL COMMENT '检查数值',
                                `report_image` varchar(255) DEFAULT NULL COMMENT '报告图片路径',
                                `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                KEY `check_item_id` (`check_item_id`),
                                KEY `idx_check_date` (`check_date`),
                                CONSTRAINT `check_record_ibfk_1` FOREIGN KEY (`check_item_id`) REFERENCES `check_item` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检查记录表';

-- ----------------------------
-- Table structure for medication_record
-- ----------------------------
DROP TABLE IF EXISTS `medication_record`;
CREATE TABLE `medication_record` (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `medicine_id` bigint DEFAULT NULL COMMENT '关联药品ID',
                                     `schedule_id` bigint DEFAULT NULL COMMENT '关联规律ID',
                                     `scheduled_time` datetime NOT NULL COMMENT '计划服药时间',
                                     `actual_time` datetime DEFAULT NULL COMMENT '实际服药时间',
                                     `dosage` decimal(5,2) DEFAULT NULL COMMENT '剂量',
                                     `status` enum('PENDING','TAKEN','MISSED') DEFAULT 'PENDING' COMMENT '状态',
                                     `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                     PRIMARY KEY (`id`),
                                     KEY `medicine_id` (`medicine_id`),
                                     KEY `schedule_id` (`schedule_id`),
                                     KEY `idx_scheduled_time` (`scheduled_time`),
                                     KEY `idx_status` (`status`),
                                     CONSTRAINT `medication_record_ibfk_1` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`id`) ON DELETE CASCADE,
                                     CONSTRAINT `medication_record_ibfk_2` FOREIGN KEY (`schedule_id`) REFERENCES `medication_schedule` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服药记录表';

-- ----------------------------
-- Table structure for medication_schedule
-- ----------------------------
DROP TABLE IF EXISTS `medication_schedule`;
CREATE TABLE `medication_schedule` (
                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                       `medicine_id` bigint DEFAULT NULL COMMENT '关联药品ID',
                                       `schedule_type` enum('DAILY','INTERVAL','WEEKLY','COURSE') NOT NULL COMMENT '规律类型',
                                       `times_config` json DEFAULT NULL COMMENT '每日时间配置，如["08:00", "20:00"]',
                                       `dosage_config` json DEFAULT NULL COMMENT '剂量配置，如{"08:00": 1, "20:00": 0.5}',
                                       `interval_days` int DEFAULT '1' COMMENT '间隔天数（INTERVAL类型使用）',
                                       `week_days` varchar(20) DEFAULT NULL COMMENT '周几服药（WEEKLY类型，如"1,3,5"）',
                                       `start_date` date DEFAULT NULL COMMENT '疗程开始日期',
                                       `end_date` date DEFAULT NULL COMMENT '疗程结束日期（可为空表示长期）',
                                       `is_active` tinyint DEFAULT '1' COMMENT '是否启用',
                                       `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                       `name` varchar(255) DEFAULT NULL,
                                       `schedule_time` varchar(50) DEFAULT NULL,
                                       PRIMARY KEY (`id`),
                                       KEY `medicine_id` (`medicine_id`),
                                       CONSTRAINT `medication_schedule_ibfk_1` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服药规律表';

-- ----------------------------
-- Table structure for medicine
-- ----------------------------
DROP TABLE IF EXISTS `medicine`;
CREATE TABLE `medicine` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `name` varchar(100) NOT NULL COMMENT '药品名称',
                            `specification` varchar(50) DEFAULT NULL COMMENT '规格，如1mg',
                            `stock_quantity` int DEFAULT '0' COMMENT '当前库存数量',
                            `per_box_quantity` int DEFAULT '0' COMMENT '每盒数量',
                            `purchase_date` date DEFAULT NULL COMMENT '最近购药日期',
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药品表';

-- ----------------------------
-- Table structure for schedule_medicine
-- ----------------------------
DROP TABLE IF EXISTS `schedule_medicine`;
CREATE TABLE `schedule_medicine` (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `schedule_id` bigint NOT NULL COMMENT '计划ID',
                                     `medicine_id` bigint NOT NULL COMMENT '药品ID',
                                     `dosage` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '剂量（片/粒）',
                                     `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_schedule_medicine` (`schedule_id`,`medicine_id`),
                                     KEY `medicine_id` (`medicine_id`),
                                     CONSTRAINT `schedule_medicine_ibfk_1` FOREIGN KEY (`schedule_id`) REFERENCES `medication_schedule` (`id`) ON DELETE CASCADE,
                                     CONSTRAINT `schedule_medicine_ibfk_2` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计划药品关联表';

SET FOREIGN_KEY_CHECKS = 1;

-- 插入预设检查项目
INSERT INTO check_item (name, unit, reference_min, reference_max, is_preset, sort_order) VALUES
('肌酐', 'μmol/L', 44.00, 133.00, 1, 1),
('eGFR', 'mL/min/1.73m²', 90.00, 120.00, 1, 2),
('他克莫司浓度', 'ng/mL', 5.00, 8.00, 1, 3),
('尿蛋白', 'mg/24h', 0.00, 150.00, 1, 4),
('白细胞', '×10⁹/L', 4.00, 10.00, 1, 5),
('血红蛋白', 'g/L', 120.00, 160.00, 1, 6),
('血小板', '×10⁹/L', 100.00, 300.00, 1, 7),
('尿酸', 'μmol/L', 150.00, 420.00, 1, 8),
('空腹血糖', 'mmol/L', 3.90, 6.10, 1, 9),
('糖化血红蛋白', '%', 4.00, 6.00, 1, 10);
