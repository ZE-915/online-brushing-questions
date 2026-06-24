CREATE TABLE IF NOT EXISTS user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  email VARCHAR(100),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_user_account_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS subject (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_subject_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT uk_subject_user_name UNIQUE (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS knowledge_point (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_kp_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_kp_subject FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE CASCADE,
  CONSTRAINT uk_kp_subject_name UNIQUE (subject_id, name),
  INDEX idx_kp_user_subject (user_id, subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  knowledge_point_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  stem LONGTEXT NOT NULL,
  options_json JSON,
  answer LONGTEXT NOT NULL,
  analysis LONGTEXT,
  difficulty TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_question_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_question_subject FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE RESTRICT,
  CONSTRAINT fk_question_kp FOREIGN KEY (knowledge_point_id) REFERENCES knowledge_point(id) ON DELETE RESTRICT,
  INDEX idx_question_filters (user_id, subject_id, knowledge_point_id, type, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exam_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  mode VARCHAR(20) NOT NULL,
  total_count INT NOT NULL,
  correct_count INT NOT NULL,
  duration_seconds INT,
  start_time DATETIME,
  end_time DATETIME,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_exam_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  INDEX idx_exam_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS answer_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  exam_record_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  user_answer LONGTEXT,
  correct_status TINYINT NOT NULL COMMENT '0 wrong, 1 correct, 2 self-evaluated',
  answer_seconds INT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_answer_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_answer_exam FOREIGN KEY (exam_record_id) REFERENCES exam_record(id) ON DELETE CASCADE,
  CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
  INDEX idx_answer_question (user_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS error_book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  wrong_count INT NOT NULL DEFAULT 1,
  marked TINYINT(1) NOT NULL DEFAULT 0,
  note TEXT,
  last_wrong_time DATETIME,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_error_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_error_question FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
  CONSTRAINT uk_error_user_question UNIQUE (user_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS knowledge_point_stat (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  knowledge_point_id BIGINT NOT NULL,
  total_questions INT NOT NULL DEFAULT 0,
  correct_count INT NOT NULL DEFAULT 0,
  mastery_degree DECIMAL(5,2) NOT NULL DEFAULT 0,
  last_test_time DATETIME,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_stat_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_stat_kp FOREIGN KEY (knowledge_point_id) REFERENCES knowledge_point(id) ON DELETE CASCADE,
  CONSTRAINT uk_stat_user_kp UNIQUE (user_id, knowledge_point_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
