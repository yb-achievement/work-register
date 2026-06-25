CREATE DATABASE IF NOT EXISTS work_register DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE work_register;

CREATE TABLE IF NOT EXISTS work_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  topic VARCHAR(20) NOT NULL COMMENT '工作主题：数据、系统功能、常规性工作',
  content TEXT NOT NULL COMMENT '工作内容',
  executor VARCHAR(50) NOT NULL COMMENT '执行人',
  due_date DATE NOT NULL COMMENT '要求完成时间',
  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '工作状态：pending、done',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
  completed_at DATETIME NULL COMMENT '确认完成时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  deleted_at DATETIME NULL COMMENT '删除时间，空表示未删除',
  INDEX idx_work_status_due_date (status, due_date),
  INDEX idx_work_created_at (created_at),
  INDEX idx_work_topic (topic),
  INDEX idx_work_executor (executor),
  INDEX idx_work_deleted_at (deleted_at),
  CONSTRAINT chk_work_topic CHECK (topic IN ('数据', '系统功能', '常规性工作')),
  CONSTRAINT chk_work_status CHECK (status IN ('pending', 'done'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作记录表';

CREATE TABLE IF NOT EXISTS work_summary_snapshots (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  summary_type VARCHAR(20) NOT NULL COMMENT '总结类型：week、month、year',
  period_start DATE NOT NULL COMMENT '总结开始日期',
  period_end DATE NOT NULL COMMENT '总结结束日期',
  completed_text TEXT NULL COMMENT '本期已完成工作总结文字',
  next_work_text TEXT NULL COMMENT '下期工作总结文字',
  model_type VARCHAR(30) NOT NULL DEFAULT 'rule' COMMENT '生成方式：rule、ai',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT chk_summary_type CHECK (summary_type IN ('week', 'month', 'year')),
  CONSTRAINT chk_model_type CHECK (model_type IN ('rule', 'ai'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作总结快照表';
