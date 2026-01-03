-- 创建数据库
CREATE DATABASE IF NOT EXISTS medication_tracker DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medication_tracker;

-- 药品表
CREATE TABLE IF NOT EXISTS medicine (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '药品名称',
    specification VARCHAR(50) COMMENT '规格，如1mg',
    stock_quantity INT DEFAULT 0 COMMENT '当前库存数量',
    per_box_quantity INT DEFAULT 0 COMMENT '每盒数量',
    purchase_date DATE COMMENT '最近购药日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品表';

-- 服药计划表（时间节点）
CREATE TABLE IF NOT EXISTS medication_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '计划名称，如"早间服药"',
    schedule_time TIME NOT NULL COMMENT '服药时间，如08:00',
    schedule_type ENUM('DAILY', 'INTERVAL', 'WEEKLY') NOT NULL DEFAULT 'DAILY' COMMENT '规律类型',
    interval_days INT DEFAULT 1 COMMENT '间隔天数（INTERVAL类型使用）',
    week_days VARCHAR(20) COMMENT '周几服药（WEEKLY类型，如"1,3,5"）',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期（可为空表示长期）',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服药计划表';

-- 计划药品关联表（一个计划包含多种药品）
CREATE TABLE IF NOT EXISTS schedule_medicine (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL COMMENT '计划ID',
    medicine_id BIGINT NOT NULL COMMENT '药品ID',
    dosage DECIMAL(5,2) NOT NULL DEFAULT 1 COMMENT '剂量（片/粒）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES medication_schedule(id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE CASCADE,
    UNIQUE KEY uk_schedule_medicine (schedule_id, medicine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划药品关联表';

-- 服药记录表
CREATE TABLE IF NOT EXISTS medication_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL COMMENT '关联计划ID',
    scheduled_time DATETIME NOT NULL COMMENT '计划服药时间',
    actual_time DATETIME COMMENT '实际服药时间',
    status ENUM('PENDING', 'TAKEN', 'MISSED') DEFAULT 'PENDING' COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES medication_schedule(id) ON DELETE CASCADE,
    INDEX idx_scheduled_time (scheduled_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服药记录表';

-- 检查项目表
CREATE TABLE IF NOT EXISTS check_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '检查项名称',
    unit VARCHAR(20) COMMENT '单位',
    reference_min DECIMAL(10,2) COMMENT '参考范围下限',
    reference_max DECIMAL(10,2) COMMENT '参考范围上限',
    is_preset TINYINT DEFAULT 0 COMMENT '是否预设项目',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查项目表';

-- 检查记录表
CREATE TABLE IF NOT EXISTS check_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_item_id BIGINT NOT NULL COMMENT '检查项ID',
    check_date DATE NOT NULL COMMENT '检查日期',
    value DECIMAL(10,2) NOT NULL COMMENT '检查数值',
    report_image VARCHAR(255) COMMENT '报告图片路径',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (check_item_id) REFERENCES check_item(id) ON DELETE CASCADE,
    INDEX idx_check_date (check_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查记录表';

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
